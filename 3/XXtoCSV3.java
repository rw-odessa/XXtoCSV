/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xxtocsv3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
//import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
//import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ua053903 Утилита конвертации отчетов РС-Банк в сsv формат. Версия
 * 1.0.5 Время создания 9:32 18.12.2015
 */
public class XXtoCSV3 {

    /**
     * Возвращает строку,
     * если строка помечена RAW, возвращается без изменений,
     * если строка помечена DATA, возвращается без изменений если строка похожа на дату,
     * если строка помечена NUMBER, то очищает от посторонних символов и меняет "." на ",",
     * если строка помечена STRING, то добавляет непечатный символ в конце,
     * чтобы Excel не интерпретировал счета как числа.
     * 
     * @param {String} sNumberMarker маркер какого типа данные в строке.
     * @param {String} sStr строка для очистки от нежелательных символов если, ее формат NUMBER.
     * @return {String}.
     */
    static String NumberOrString(String sNumberMarker, String sStr) {
        String sCurStr, sMarker;
        sCurStr = sStr;
        sMarker = sNumberMarker;
        
        //RAW
        if (sMarker.equalsIgnoreCase("RAW")) {
            return sCurStr;
        }
        
        //DATA
        if (sMarker.equalsIgnoreCase("DATA")) {
            //length() < 4 or Undefined or -1 or 0
            if ((sCurStr.trim().length() < 4 )
             || (sCurStr.trim().equals("Undefined"))) {
                return "";
            } else {
                return sCurStr.trim();
            }
        }
        
        //NUMBER
        if (sMarker.equalsIgnoreCase("NUMBER")) {
            //System.out.println("Output[k] = " + Output[k]);
            sCurStr = sCurStr.replaceAll("[^0-9^\\-^\\.]*", "");
            sCurStr = sCurStr.replaceAll("\\.", ",");
            return sCurStr;
        //STRING
        } else {
            // !!!Временно.
            // Если это не число то добавим в конце пробел, чтобы CSV однозначно интерпретировался строкой.
            sCurStr = sCurStr + " "; //Пробел не простой.
            return sCurStr;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        String FlIn, FlOut, FlCfg; //Имена файлов для обработки
        long StartTime, EndTime; //Время начала окончания работы

        StartTime = System.currentTimeMillis();

        //Опредиление структуры параметров командной строки
        switch (args.length) {
            case 2:
                FlIn = args[0];
                FlCfg = args[1];
                FlOut = FlIn.replaceFirst("[.][^.]+$", "") + "-out.csv";
                break;
            case 3:
                FlIn = args[0];
                FlCfg = args[1];
                FlOut = args[1];
                break;
            default:
                //FlIn = ".\\ReportA7.160";
                //FlIn = ".\\ReportF8.102";
                //FlIn = ".\\ReportD5.001";
                //FlIn = ".\\ReportD6.002";
                //FlCfg = ".\\d6.csv";
                //FlOut = FlIn.replaceFirst("[.][^.]+$", "") + ".csv";
                //ReportA7.023 A7.csv
                FlIn = "";
                FlCfg = "";
                FlOut = "";
                System.out.println("Info: Need two or three arguments REPORT file, CONFIG file, OUTPUT file");
                System.out.println("Example: java.exe -jar XXtoCSV.jar ReportA7.104 A7.csv");
                System.out.println("Example: java.exe -jar XXtoCSV.jar ReportF8.102 F8.csv F8tabOUT.csv");
                System.exit(1);
        }

        //Проверка существования входного, выходного и файла конфигурации.
        //InFileName
        File f = new File(FlIn);
        if (!f.exists()) {
            System.out.println("ERROR, file - " + FlIn + " not found");
            System.exit(1);
        }

        //CfgFileName
        f = new File(FlCfg);
        if (!f.exists()) {
            System.out.println("ERROR, file - " + FlCfg + " not found");
            System.exit(1);
        }

        //OutFileName
        f = new File(FlOut);
        //if (args.length == 1 && f.exists()) {
        while (f.exists()) {
            Calendar calendar = Calendar.getInstance();
            FlOut = String.format(FlIn.replaceFirst("[.][^.]+$", "") + "_%1$tY%1$te%1$tm-%1$tH%1$tM%1$tS.csv", calendar);
            f = new File(FlOut);
        }
        String str; //текущая строка прочитанная из файла.
        int StrCount = 1;

        //Сообщение о файлах
        System.out.println("Report File - " + FlIn);
        System.out.println("Output File - " + FlOut);
        System.out.println("Config File - " + FlCfg);

        //---------------------------------------------------
        // Прочитаем все строки в ArrayList файла настроек
        ArrayList<String> cfg = new ArrayList();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(FlCfg)), "UTF-8"))) {
            while ((str = br.readLine()) != null) {
                cfg.add(str);
                //System.out.println(str);
            }
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            System.out.println("FileNotFoundException - " + e);
            System.exit(1);
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("IOException - " + e);
            System.exit(1);
        }

        cfg.remove(0); //Удалим заглавие конфигурационного файла.

        String[] cell; //массив текущих значений настроек, получаемой из CSV строки методом split;
        String[] Col = new String[cfg.size()]; //массив поисковых значений, получаемый при обработке файла настроек;
        int[] ColPos = new int[cfg.size()]; //массив настроек значений начала строки ячейки;
        int[] ColLen = new int[cfg.size()]; //массив настроек значений усечения длинны строки ячейки;
        int[] Parent = new int[cfg.size()]; //массив настроек родительского отношения ячейки;
        int[] Relativ = new int[cfg.size()]; //массив настроек относительного положения ячейки;
        int[] Multiline = new int[cfg.size()]; //массив настроек признаков многострочных ячеек;
        String[] Output = new String[cfg.size()]; //массив текущих значений ячейки для печати, первоначально инициируется названиями столбцов;
        String[] Output2 = new String[cfg.size()]; //вспомогательный массив текущих значений ячейки для печати относительно расположенных ячеек;
        String[] Sinonim, Sinonim2, Sinonim3; //массивы значений синонимов ключей поиска
        String[] Format = new String[cfg.size()]; //массив формата столбца NUMBER (number), или другое.
        String[] PreValue = new String[cfg.size()]; //массив предшествующих значений
        String[] ForvardValue = new String[cfg.size()]; //массив последующих значений, если поиск идет вперед.

        // Прочитаем все строки из ArrayList в массивы настроек
        for (int i = 0; i < cfg.size(); i++) {
            cell = cfg.get(i).split(";");

            //проверка установки
            if (cell.length != 11) {
                System.out.println("Config file error - " + FlCfg);
                System.out.println("String - " + (i + 2));
                System.exit(1);
            }
            //System.out.println("cell - " + cfg.get(i));
            //System.out.println("cell[i] - " + cell[0] + "\t" + cell[1] + "\t" + cell[2] + "\t" + cell[3] + "\t" + cell[4]);

            //заполним массив поисковых значений для столбцов.
            Col[i] = cell[2];

            //заполним массив значений начала строки ячейки
            try {
                ColPos[i] = Integer.parseInt(cell[3]);
            } catch (NumberFormatException e) {
                System.out.println("NumberFormatException - " + e);
                System.out.println("Config file error - " + FlCfg);
                System.out.println("String - " + (i + 2) + " cell[4]");
                ColPos[i] = 0;
            }

            //заполним массив значений длинны столбца
            try {
                ColLen[i] = Integer.parseInt(cell[4]);
            } catch (NumberFormatException e) {
                System.out.println("NumberFormatException - " + e);
                System.out.println("Config file error - " + FlCfg);
                System.out.println("String - " + (i + 2) + " cell[5]");
                ColLen[i] = 0;
            }

            //заполним массив родительского отношения столбца
            try {
                Parent[i] = Integer.parseInt(cell[5]) - 1;
            } catch (NumberFormatException e) {
                System.out.println("NumberFormatException - " + e);
                System.out.println("Config file error - " + FlCfg);
                System.out.println("String - " + (i + 2) + " cell[6]");
                Parent[i] = 0;
            }

            //заполним массив относительного положения ячейки
            try {
                Relativ[i] = Integer.parseInt(cell[6]);
            } catch (NumberFormatException e) {
                System.out.println("NumberFormatException - " + e);
                System.out.println("Config file error - " + FlCfg);
                System.out.println("String - " + (i + 2) + " cell[7]");
                Relativ[i] = 0;
            }

            //массив настроек признаков многострочных ячеек;
            try {
                Multiline[i] = Integer.parseInt(cell[7]);
            } catch (NumberFormatException e) {
                System.out.println("NumberFormatException - " + e);
                System.out.println("Config file error - " + FlCfg);
                System.out.println("String - " + (i + 2) + " cell[8]");
                Multiline[i] = 0;
            }

            //заполним массивы для печати ячейки.
            if (Relativ[i] < 0) {
                //если относительного положения ячейки перед основной
                Output2[i] = cell[1];
                Output[i] = "-";
            } else {
                //если относительного положения ячейки следом за основной
                Output[i] = cell[1];
                Output2[i] = "-";
            }

            //массив настроек формата поля столбца;
            Format[i] = cell[8];

            //массив настроек предыдущих значений столбца;
            PreValue[i] = cell[9];
//            try {
//                PreValue[i] = Integer.parseInt(cell[9]) - 1; //-1 по причине удобства для пользователя 
//            } catch (NumberFormatException e) {
//                System.out.println("NumberFormatException - " + e);
//                System.out.println("Config file error - " + FlCfg);
//                System.out.println("String - " + (i + 2) + " cell[9]");
//                PreValue[i] = 0;
//            }

            //массив настроек предыдущих значений столбца;
            ForvardValue[i] = cell[10];

            //System.out.println(Output[i] + "\t" + Col[i] + "\t" + ColPos[i] + "\t" + ColLen[i] + "\t" + Parent[i] + "\t" + Relativ[i]);
        } // END for (int i = 0; i < cfg.size(); i++)

        //System.exit(1);
        //System.out.println("Start");
        System.out.println("Config File Read - OK");


        Map< String, String> keys = new HashMap<>(); //массив текущих найденных значений по ключевым словам, который не обнуляется.
//        for (int i = 0; i < Col.length; i++) {
//            Sinonim = Col[i].split("<@>"); //если строка имеет синонимы разобьем по синонимам.
//            for (int l = 0; l < Sinonim.length; l++) {
//                if (!keys.containsKey(Sinonim[l])) {
//                    keys.put(Sinonim[l], "-");
//                    System.out.println(Sinonim[l] + "..." + keys.get(Sinonim[l]));
//                }
//            }
//        }
//        System.exit(1);

        //---------------------------------------------------
        // Прочитаем все строки в ArrayList с учетом кодировки файла.
        ArrayList<String> al = new ArrayList();
        try (BufferedReader inp = new BufferedReader(new InputStreamReader(new FileInputStream(FlIn), "Cp866"))) {
            while ((str = inp.readLine()) != null) {
                //al.add(str);
                al.add(str.replaceAll(";", ","));
            }
        } catch (UnsupportedEncodingException e) {
            System.out.println("UnsupportedEncodingException - " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.out.println("IOException - " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.out.println("Exception - " + e.getMessage());
            System.exit(1);
        }


        //создадим поток записи файла отчета
        try (PrintWriter pw = new PrintWriter(new File(FlOut), "Cp1251")) {
            int SubStrStart, SubStrEnd, ColCurr = 0; //переменные границ вырезания строк
            boolean StrContains = false;
            int Marker = 0; //маркер родительского поля.

            //Цикл чтения строк исходного файла
            for (int a = 0; a < al.size(); a++) {

                //поиск в строке нужного критерия
                for (int i = 0; i < Col.length; i++) {

                    if (i == 0) {
                        StrContains = false;
                        //ColCurr = 0;
                    }

                    Sinonim = Col[i].split("<@>"); //если строка имеет синонимы разобьем по синонимам.
                    for (int l = 0; l < Sinonim.length; l++) {
                        //System.out.println(Col[i] + "   " + l + "   " + Sinonim[l]);
                        //проверяем наличие ключа в строке для отбора.
                        //if (str.contains(Col[i])) {
                        if (al.get(a).contains(Sinonim[l])) {
                            ColCurr = i; // сохраним текущее значение поисковой строки, потому как оно может быть не уникально
                            StrContains = true;
                            //keys.put(Sinonim[l], "-");

                            //Вывод в файл строки, по началу новой записи.
                            if (i == 0) {
                                for (int k = 0; k < Output.length; k++) {

//                                  //Заполнение пустых значений в таблице придшествующими значениями
                                    //System.out.print(Output[k] + ";");
                                    if (Output[k].equals("-") && !PreValue[k].equals("-") && !PreValue[k].equalsIgnoreCase("x")) {
                                        Output[k] = keys.get(PreValue[k]);
                                        //System.out.print(PreValue[k] + ">>>" + keys.get(PreValue[k]));
                                    }

                                    //Если поле не заполнено, то ищем его вперед
                                    //пока не встретится отбой по строке массива ForvardValue[]
                                    if (Output[k].equals("-") && !ForvardValue[k].equals("-")) {
                                        //&& ForvardValue[k].equals(keys.get("found_key_" + Integer.toString(k)))
                                        //System.out.println("found_key_" + Integer.toString(k) + ">>>" + keys.get("found_key_" + Integer.toString(k)));

                                        boolean found_key = false;
                                        //int m = 0;
                                        //while (!found_key) {
                                        for (int m = 0; m < Parent.length; m++) {
                                            //System.out.println(keys.get("found_key_" + Integer.toString(m)));
                                            found_key = found_key || ForvardValue[k].equals(keys.get("found_key_" + Integer.toString(m)));
                                            //m++;
                                        }

                                        if (found_key) {
                                            int b = a - 2;
                                            while (!al.get(b).contains(ForvardValue[k])) {
                                                b--;
                                            }
                                            b++;
                                            while (!al.get(b).contains(ForvardValue[k])) {
                                                Sinonim3 = Col[k].split("<@>"); //если строка имеет синонимы разобьем по синонимам.
                                                for (int c = 0; c < Sinonim3.length; c++) {
                                                    if (al.get(b).contains(Sinonim3[c])) {

                                                        SubStrStart = al.get(b).indexOf(Sinonim3[c]) + Sinonim3[c].length() + ColPos[k];
                                                        SubStrEnd = SubStrStart + ColLen[k];
                                                        if (SubStrEnd > al.get(b).length()) {
                                                            SubStrEnd = al.get(b).length();
                                                        }
                                                        if (SubStrStart > al.get(b).length()) {
                                                            SubStrStart = al.get(b).length();
                                                        }
                                                        Output[k] = al.get(b).substring(SubStrStart, SubStrEnd).trim();
                                                        //Если поле содержит число, то необходим очитстить, от не желательных символов
                                                        Output[k] = NumberOrString(Format[k], Output[k]);
                                                        //System.out.println("Output[k] = " + Output[k]);
                                                    }
                                                }
                                                b++;
                                            }
                                        }
                                    }

                                    //Если запись относится к полю идущему перед первым
                                    //Значит значение поля Relativ[k] < 0
                                    //Печатаем предидущее поле
                                    if (Relativ[k] < 0) {
                                        pw.print(Output2[k] + ";");
                                        Output2[k] = Output[k];
                                        //System.out.println("Output2[k] = " + Output2[k]);
                                    } else {
                                        pw.print(Output[k] + ";");
                                    }
                                    Output[k] = "-";
                                }
                                pw.println();
//                                System.out.println("==========================================");
//                                for (int m = 0; m < Parent.length; m++) {
//                                    System.out.println(keys.get("found_key_" + Integer.toString(m)));
//                                }
                            }

                            //Проверка какого типа поле
                            if (Parent[i] < 0) { //Родительское
                                //SubStrStart = str.indexOf(Col[i]) + Col[i].length() + ColPos[i];
                                //Sinonim[l]
                                SubStrStart = al.get(a).indexOf(Sinonim[l]) + Sinonim[l].length() + ColPos[i];
                                SubStrEnd = SubStrStart + ColLen[i];
                                if (SubStrEnd > al.get(a).length()) {
                                    SubStrEnd = al.get(a).length();
                                }
                                if (SubStrStart > al.get(a).length()) {
                                    SubStrStart = al.get(a).length();
                                }
                                if (Output[i].equals("-")) {
                                    Output[i] = al.get(a).substring(SubStrStart, SubStrEnd).trim();
                                    //Если поле содержит число, то необходим очитстить, от не желательных символов
                                    Output[i] = NumberOrString(Format[i], Output[i]);
                                }
                                Marker = i;
                                // Если не запрещено писать историческое значение запишем его
                                // почему его иногда надо запрещать
                                // по причине того, что оно может перенакрывать другие значение
                                // если ключи поиска одинаковы
                                if (!PreValue[i].equalsIgnoreCase("x")) {
                                    keys.put(Sinonim[l], Output[i]);
                                }
                                // запишем что мы в прошлый раз нашли
                                keys.put("found_key_" + Integer.toString(i), Sinonim[l]);


                                //System.out.println("Marker = " + Marker);
                            } else { //Дочернее
                                for (int m = 0; m < Parent.length; m++) {
                                    //System.out.println("ColCurr = " + ColCurr + " " + Col[m].equals(Sinonim[l]) + " " +Parent[m]);

                                    Sinonim2 = Col[m].split("<@>");
                                    for (int t = 0; t < Sinonim2.length; t++) {

                                        //if (Marker == Parent[m] && Col[m].equals(Col[i])) {
                                        if (Marker == Parent[m] && Sinonim2[t].equals(Sinonim[l])) {

                                            //if (Marker == Parent[m] && Col[m].equals(ColCurr)) {
                                            //if (Marker == Parent[m] && str.contains(Col[m])) {
                                            //if (Marker == Parent[m]) {
                                            //SubStrStart = str.indexOf(Col[m]) + Col[m].length() + ColPos[m];

                                            //Sinonim[l]
                                            SubStrStart = al.get(a).indexOf(Sinonim[l]) + Sinonim[l].length() + ColPos[m];
                                            SubStrEnd = SubStrStart + ColLen[m];
                                            if (SubStrEnd > al.get(a).length()) {
                                                SubStrEnd = al.get(a).length();
                                            }
                                            if (SubStrStart > al.get(a).length()) {
                                                SubStrStart = al.get(a).length();
                                            }
                                            if (Output[m].equals("-")) {
                                                Output[m] = al.get(a).substring(SubStrStart, SubStrEnd).trim();
                                                //Если поле содержит число, то необходим очистить, от нежелательных символов
                                                Output[m] = NumberOrString(Format[m], Output[m]);

                                            }
                                            // Если не запрещено писать историческое значение запишем его
                                            // почему его иногда надо запрещать
                                            // по причине того, что оно может перенакрывать другие значение
                                            // если ключи поиска одинаковы
                                            if (!PreValue[m].equalsIgnoreCase("x")) {
                                                keys.put(Sinonim2[t], Output[m]);
                                            }
                                            // запишем что мы в прошлый раз нашли
                                            keys.put("found_key_" + Integer.toString(m), Sinonim2[t]);
                                        }
                                    }
                                } //for (int t = 0; l < Sinonim.length; t++) {
                            }
                            //System.out.println(Output[i]);
                        }
                    }
                }

                //печать строки к последнему найденному полю, пока не найдется новое поле.
                if (!StrContains) {
                    if (Multiline[ColCurr] > 0) {
                        Output[ColCurr] = Output[ColCurr] + al.get(a).trim();
                    }
                }
                StrCount++; //считаем строки.
            }
            //распечатаем последнюю строку
            for (int k = 0; k < Output.length; k++) {
                //System.out.print(Output[k] + ";");
                //pw.print(Output[k] + ";");

                if (Output[k].equals("-") && !PreValue[k].equals("-")) {
                    Output[k] = keys.get(PreValue[k]);
                    //System.out.print(PreValue[k] + ">>>" + keys.get(PreValue[k]));
                }

                if (Relativ[k] < 0) {
                    pw.print(Output2[k] + ";");
                    Output2[k] = Output[k];
                } else {
                    pw.print(Output[k] + ";");
                }
                //Output[k] = "-";
                //System.out.print(PreValue[k] + ">>>" + keys.get(PreValue[k]));
            }
            pw.println();

        } catch (Exception e) {
            System.out.println("PrintWriter Exception - " + e.getMessage());
            System.out.println("Error string - " + StrCount);
            System.exit(1);
        }

        System.out.println("Processing " + StrCount + " lines.");
        System.out.println("Convert report file " + FlIn + " to CSV " + FlOut + " - OK");
        EndTime = System.currentTimeMillis();
        System.out.println("Elapsed time - " + (EndTime - StartTime) / 1000 + " sec");
        System.exit(0);
    }
}
