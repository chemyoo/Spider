@echo off

echo ����������...

cd %~dp0

java -jar Spider.jar

echo ���������˳�...

ping -n 3 127.1 >nul

exit