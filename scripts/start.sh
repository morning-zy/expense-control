#!/bin/bash
# 启动脚本：后台运行 jar，日志写到 logs/app.log，pid 写到 logs/app.pid
#
# 用法：
#   ./start.sh           正常启动（8081）
#   ./start.sh 8082      换端口
#   JWT_SECRET=xxx ./start.sh   用环境变量覆盖密钥（生产必须这样）
#
# 面向 Windows + Git Bash。Linux 上同样的脚本可以直接用。

set -e

PORT="${1:-8081}"
APP_HOME="$(cd "$(dirname "$0")/.." && pwd)"
JAR="$APP_HOME/target/expense-control-0.0.1-SNAPSHOT.jar"
LOG_DIR="$APP_HOME/logs"
PID_FILE="$LOG_DIR/app.pid"

# Git Bash 传的是 /e/xxx 这种路径，java.exe 不认；转成 E:/xxx
# cygpath 只在 Git Bash / MSYS 里存在，Linux 上不存在，所以要先判断
if command -v cygpath >/dev/null 2>&1; then
    JAR_WIN="$(cygpath -w "$JAR")"
    LOG_WIN="$(cygpath -w "$LOG_DIR/app.log")"
else
    JAR_WIN="$JAR"
    LOG_WIN="$LOG_DIR/app.log"
fi

# 找不到 java 就报错退出，别让它跑到一半才发现
if ! command -v java >/dev/null 2>&1; then
    echo "[ERROR] 找不到 java，请把 JDK 21 的 bin 目录加进 PATH" >&2
    exit 1
fi

if [ ! -f "$JAR" ]; then
    echo "[ERROR] 没找到 $JAR" >&2
    echo "        先执行：mvn clean package -DskipTests" >&2
    exit 1
fi

# 已经在跑就直接退出，避免两个进程抢同一个端口
if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat "$PID_FILE")
    if ps -p "$OLD_PID" > /dev/null 2>&1; then
        echo "[INFO] 已经在运行，PID=$OLD_PID。要重启请先 ./stop.sh"
        exit 0
    fi
    rm -f "$PID_FILE"
fi

mkdir -p "$LOG_DIR"

echo "[INFO] 启动 expense-control，端口 $PORT"

# JWT 密钥优先取环境变量，没设才用 application.yml 里的开发值
JWT_ARG=""
if [ -n "$JWT_SECRET" ]; then
    JWT_ARG="--app.jwt.secret=$JWT_SECRET"
    echo "[INFO] 使用环境变量 JWT_SECRET 覆盖签名密钥"
fi

nohup java -jar "$JAR_WIN" \
    --server.port="$PORT" \
    --spring.profiles.active=dev \
    $JWT_ARG \
    >> "$LOG_WIN" 2>&1 &

echo $! > "$PID_FILE"
echo "[INFO] PID=$(cat "$PID_FILE")，日志：$LOG_DIR/app.log"

# 等就绪：最多等 30 秒，就绪了立刻返回，方便写进 CI
for i in $(seq 1 30); do
    if curl -s -m 2 "http://localhost:$PORT/api/ping" > /dev/null 2>&1; then
        echo "[INFO] 启动成功：http://localhost:$PORT/api/ping"
        exit 0
    fi
    sleep 1
done

echo "[WARN] 30 秒内没就绪，请查看日志：tail -50 $LOG_DIR/app.log" >&2
exit 1
