#!/bin/bash

VERSION="1.1"

# 获取脚本所在的目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 帮助函数
show_help() {
    echo "Usage: $0 [options] [command]"
    echo "Options:"
    echo "  -h, --help     Show this help message"
    echo "  -v, --version  Show version information"
    echo "  -s, --script   Specify the script to run (default: auto-detect)"
    echo "Commands:"
    echo "  start          Start the server (default)"
    echo "  stop           Stop the server"
    echo "  reload         Reload the server"
    echo "  [any]          Any other command to pass to the script"
}

# 版本信息函数
show_version() {
    echo "runWithServer.sh version $VERSION"
}

# 查找脚本函数
find_script() {
    local script_files=($(find "$SCRIPT_DIR" -maxdepth 1 -type f -name "*.sh" ! -name "$(basename "$0")"))
    if [ ${#script_files[@]} -eq 0 ]; then
        echo "Error: No .sh files found in the current directory." >&2
        exit 1
    elif [ ${#script_files[@]} -gt 1 ]; then
        echo "Error: Multiple .sh files found. Please specify using -s option:" >&2
        for file in "${script_files[@]}"; do
            echo "  $(basename "$file")" >&2
        done
        exit 1
    fi
    echo "$(basename "${script_files[0]}")"
}

# 默认值
TARGET_SCRIPT=""
PARAM="start"

# 解析命令行参数
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -v|--version)
            show_version
            exit 0
            ;;
        -s|--script)
            TARGET_SCRIPT="$2"
            shift 2
            ;;
        *)
            PARAM="$1"
            shift
            ;;
    esac
done

# 如果没有指定脚本，自动查找
if [ -z "$TARGET_SCRIPT" ]; then
    TARGET_SCRIPT=$(find_script)
fi

# 检查目标脚本是否存在
if [ ! -f "$SCRIPT_DIR/$TARGET_SCRIPT" ]; then
    echo "Error: Script $TARGET_SCRIPT not found in $SCRIPT_DIR" >&2
    exit 1
fi

# 执行脚本
if command -v runuser &> /dev/null; then
    runuser -l server -c "cd '$SCRIPT_DIR' && ./$TARGET_SCRIPT $PARAM"
else
    su - server -c "cd '$SCRIPT_DIR' && ./$TARGET_SCRIPT $PARAM"
fi
