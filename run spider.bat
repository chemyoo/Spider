@echo off

echo ����������...

::����win7ϵͳ����ת����ǰ����Ŀ¼��ִ������������
cd %~dp0

java -jar Spider.jar

echo ���������˳�...

ping -n 3 127.1 >nul

exit