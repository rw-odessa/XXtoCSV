@ECHO OFF
setlocal

REM ����� JAVA �⨫��� XXtoCSV
REM V 1.01 - ����� 11:47 17.02.2015, �⪫�祭 ��室 �� �訡��, ��������� ࠧ������� ᮮ�饭�� � ����প� ��� ��ᬮ�� १���⮢.
REM V 1.00 - ��ࢠ� ��ᯫ��樮���� ����� 10:39 22.01.2015

REM==================================================
REM ��⠭���� ��६�����
SET RUNDIR=%~dp0
SET FILES_PATH=%RUNDIR%
CD "%RUNDIR%"|| ECHO %date% %time% - ERROR CD %RUNDIR%


REM==================================================
REM ������ 䠩�� NODE_NAME
REM==================================================
if exist %FILES_PATH%NODE_NAME.* (
for /f %%a IN ('dir /o:-d "%FILES_PATH%NODE_NAME.*" /b') do (

ECHO .
ECHO ==================================================
ECHO %date% %time% - FILE %%a
REM �����⨬ ��������� ������� ���������� 䠩��.

ECHO "C:\Program Files\Java\jdk1.7.0_21\bin\java.exe" -jar "%RUNDIR%XXtoCSV.jar" "%FILES_PATH%%%a" ".\TCMBackUP.csv"
"C:\Program Files\Java\jdk1.7.0_21\bin\java.exe" -jar "%RUNDIR%XXtoCSV.jar" "%FILES_PATH%%%a" ".\TCMBackUP.csv"|| (
ECHO %date% %time% - ERROR RUN XXtoCSV.jar
	REM EXIT /B 1
)
ECHO RUN XXtoCSV.jar - OK
)
)

REM==================================================
REM ����প� ���� ��� �ਭ��� �襭��.
ECHO .
ECHO ==================================================
ECHO WAIT 5 second to view result
ping 127.0.0.1 -n 10 > nul
	REM pause
EXIT /B 0
