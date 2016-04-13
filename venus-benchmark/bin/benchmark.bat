@rem ----------------------------------------------------------------------------
@rem 启动Venus的脚本
@rem
@rem 需要设置如下环境变量：
@rem
@rem    JAVA_HOME           - JDK的安装路径
@rem
@rem ----------------------------------------------------------------------------
@echo off
if "%OS%"=="Windows_NT" setlocal

:CHECK_JAVA_HOME
if not "%JAVA_HOME%"=="" goto SET_VENUS_HOME

echo
echo 错误: 必须设置环境变量“JAVA_HOME”，指向JDK的安装路径
echo
goto END

:SET_VENUS_HOME
set VENUS_HOME=%~dp0..
if not "%VENUS_HOME%"=="" goto START_VENUS

echo
echo 错误: 必须设置环境变量“VENUS_HOME”，指向Amoeba的安装路径
echo
goto END

:START_VENUS

set DEFAULT_OPTS=-server -Xms256m -Xmx256m -Xss128k
set DEFAULT_OPTS=%DEFAULT_OPTS% -XX:+HeapDumpOnOutOfMemoryError -XX:+AggressiveOpts -XX:+UseParallelGC -XX:+UseBiasedLocking -XX:NewSize=64m
set DEFAULT_OPTS=%DEFAULT_OPTS% "-Dproject.home=%VENUS_HOME%"
set DEFAULT_OPTS=%DEFAULT_OPTS% "-Dclassworlds.conf=%VENUS_HOME%\bin\benchmark.classworlds"

set JAVA_EXE="%JAVA_HOME%\bin\java.exe"
set CLASSPATH="%VENUS_HOME%\lib\plexus-classworlds-2.4.4-HEXNOVA.jar"
set MAIN_CLASS="org.codehaus.classworlds.Launcher"

%JAVA_EXE% %DEFAULT_OPTS% -classpath %CLASSPATH% %MAIN_CLASS% %*

:END
if "%OS%"=="Windows_NT" endlocal
pause
