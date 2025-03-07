FROM eclipse-temurin:8-jre
MAINTAINER kiilin <kiilin@kiilin.com>
# envs
ENV JDX_DIR=/jdx

# 工作目录
WORKDIR ${JDX_DIR}

ADD ./jdx.jar ${JDX_DIR}/app.jar
ADD ./docker-entrypoint.sh ${JDX_DIR}/docker-entrypoint.sh

# 入口文件
ENTRYPOINT ["sh", "docker-entrypoint.sh"]
