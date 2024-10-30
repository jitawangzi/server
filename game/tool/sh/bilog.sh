#!/bin/bash -l
# 取前一天的日期
file_time=$(date -d "yesterday" +%Y-%m-%d)
# 文件名信息
file_name="xy-xy_game_1-${file_time}.tgz"
log_dir=/server/game/logs/cylog/xy_game_1/

# 压缩处理
cd "${log_dir}" || exit
rm -f *.tgz
tar zcvf "${file_name}" *.log."${file_time}"

#上传压缩包
/server/app/coscli/coscli -c /server/app/coscli/cos.yaml cp "${log_dir}/${file_name}" cos://bi-bak-1301961493/game_package/party/"${file_name}"

