#!/bin/bash
set -e

if [ ! -d ${JDX_DIR}/config ]; then
  echo "没有映射config配置目录给本容器，请先映射config配置目录...\n"
  exit 1
fi

if [ ! -s ${JDX_DIR}/config/config.json ]; then
  echo "检测到config配置目录下不存在config.json，从示例文件复制一份用于初始化...\n"
  cp -fv ${JDX_DIR}/sample/config.json ${JDX_DIR}/config/config.json
fi

java -jar ${JDX_DIR}/app.jar
