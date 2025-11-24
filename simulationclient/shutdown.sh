#!/bin/bash

# 定义PID文件路径
PID_FILE="./app.pid"

# 检查PID文件是否存在
if [ ! -f "$PID_FILE" ]; then
    echo "未找到PID文件，程序可能没有运行"
    exit 1
fi

# 读取PID
PID=$(cat "$PID_FILE")

# 检查进程是否存在
if ! ps -p $PID > /dev/null 2>&1; then
    echo "进程不存在 (PID: $PID)，清理PID文件"
    rm -f "$PID_FILE"
    exit 1
fi

# 尝试优雅关闭
echo "正在停止程序 (PID: $PID)..."
kill $PID

# 等待进程结束，最多等待300秒
for i in {1..300}; do
    if ! ps -p $PID > /dev/null 2>&1; then
        echo "程序已成功停止"
        rm -f "$PID_FILE"
        exit 0
    fi
    sleep 1
done

# 如果300秒后还没停止，强制杀死
echo "程序未能正常停止，执行强制关闭..."
kill -9 $PID

sleep 1

if ! ps -p $PID > /dev/null 2>&1; then
    echo "程序已强制停止"
    rm -f "$PID_FILE"
    exit 0
else
    echo "无法停止程序，请手动处理"
    exit 1
fi
