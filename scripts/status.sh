#!/bin/bash
# 健康检查：进程 + 端口 + 接口三层都过才叫活着
APP_HOME="$(cd "$(dirname "$0")/.." && pwd)"
PID_FILE="$APP_HOME/logs/app.pid"
PORT="${1:-8081}"

if [ -f "$PID_FILE" ] && ps -p "$(cat "$PID_FILE")" > /dev/null 2>&1; then
    echo "[进程] 运行中，PID=$(cat "$PID_FILE")"
else
    echo "[进程] 未运行"
fi

RESP=$(curl -s -m 3 "http://localhost:$PORT/api/ping" 2>/dev/null)
if [ -n "$RESP" ]; then
    echo "[接口] $RESP"
else
    echo "[接口] 无响应（端口 $PORT）"
    exit 1
fi
