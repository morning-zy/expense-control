#!/bin/bash
# 停止脚本：按 pid 文件优雅停止，10 秒还没退就强杀
set -e

APP_HOME="$(cd "$(dirname "$0")/.." && pwd)"
PID_FILE="$APP_HOME/logs/app.pid"

if [ ! -f "$PID_FILE" ]; then
    echo "[INFO] 没有 pid 文件，应用可能没在运行"
    exit 0
fi

PID=$(cat "$PID_FILE")
if ! ps -p "$PID" > /dev/null 2>&1; then
    echo "[INFO] 进程 $PID 已不存在，清理 pid 文件"
    rm -f "$PID_FILE"
    exit 0
fi

echo "[INFO] 停止进程 $PID"
kill "$PID"
for i in $(seq 1 10); do
    if ! ps -p "$PID" > /dev/null 2>&1; then
        rm -f "$PID_FILE"
        echo "[INFO] 已停止"
        exit 0
    fi
    sleep 1
done

echo "[WARN] 10 秒未退出，强制结束"
kill -9 "$PID" 2>/dev/null || true
rm -f "$PID_FILE"
echo "[INFO] 已强制停止"
