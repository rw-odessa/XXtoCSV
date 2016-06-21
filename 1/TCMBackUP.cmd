@ECHO OFF
setlocal

REM Запуск JAVA итилиты XXtoCSV
REM V 1.01 - версия 11:47 17.02.2015, отключен выход при ошибке, добавлено разделение сообщений и задержка для просмотра результатов.
REM V 1.00 - Первая эксплуатационная версия 10:39 22.01.2015

REM==================================================
REM Установка переменных
SET RUNDIR=%~dp0
SET FILES_PATH=%RUNDIR%
CD "%RUNDIR%"|| ECHO %date% %time% - ERROR CD %RUNDIR%


REM==================================================
REM Найдем файлы NODE_NAME
REM==================================================
if exist %FILES_PATH%NODE_NAME.* (
for /f %%a IN ('dir /o:-d "%FILES_PATH%NODE_NAME.*" /b') do (

ECHO .
ECHO ==================================================
ECHO %date% %time% - FILE %%a
REM Запустим конвертацию каждого найденного файла.

ECHO "C:\Program Files\Java\jdk1.7.0_21\bin\java.exe" -jar "%RUNDIR%XXtoCSV.jar" "%FILES_PATH%%%a" ".\TCMBackUP.csv"
java.exe -jar "%RUNDIR%XXtoCSV.jar" "%FILES_PATH%%%a" ".\TCMBackUP.csv"|| (
ECHO %date% %time% - ERROR RUN XXtoCSV.jar
	REM EXIT /B 1
)
ECHO RUN XXtoCSV.jar - OK
)
)

REM==================================================
REM Задержка старта для принятия решения.
ECHO .
ECHO ==================================================
ECHO WAIT 5 second to view result
ping 127.0.0.1 -n 10 > nul
	REM pause
EXIT /B 0
