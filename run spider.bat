@echo off

echo 程序运行中...

::兼容win7系统，先转到当前运行目录再执行批处理命令
cd %~dp0

java -jar Spider.jar

echo 程序正在退出...

ping -n 3 127.1 >nul

exit