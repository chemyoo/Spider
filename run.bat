@echo off

echo 程序运行中...

cd %~dp0

java -jar Spider.jar

echo 程序正在退出...

ping -n 3 127.1 >nul

exit