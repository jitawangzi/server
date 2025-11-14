#!/bin/bash

# 定义PID文件路径
PID_FILE="./app.pid"

# 检查是否已经在运行
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p $PID > /dev/null 2>&1; then
        echo "程序已经在运行中，PID: $PID"
        echo "如需重启，请先执行 ./stop.sh"
        exit 1
    else
        echo "发现残留的PID文件，但进程不存在，正在清理..."
        rm -f "$PID_FILE"
    fi
fi

TOTAL_MEM=$(free -k | awk '/^Mem:/{print $2}')
#MAX_HEAP=$((TOTAL_MEM / 2 / 1024))
MAX_HEAP=12288

JAVA_HOME=$(pwd)/jdk-21.0.2

JVM_ARGS="--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED -Dio.netty.tryReflectionSetAccessible=true -Xmx${MAX_HEAP}m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=oom.dump"

APP_CLASSPATH="-cp .:./lib/*"
MAIN_CLASS=cn.game.simulation.client.ServerTestContext

nohup ${JAVA_HOME}/bin/java ${JVM_ARGS} ${APP_CLASSPATH} ${MAIN_CLASS} "$@" >log 2>&1 &

# 保存进程PID
echo $! > "$PID_FILE"

echo "程序启动成功，PID: $!"
echo "日志文件: ./log"
