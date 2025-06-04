FROM openjdk:11.0.16-jdk
MAINTAINER kiilin <kiilin@kiilin.com>
# envs
ENV JDX_DIR=/jdx

# 工作目录
WORKDIR ${JDX_DIR}


RUN cp /etc/apt/sources.list /etc/apt/sources.list.bck
ADD ./sources.list /etc/apt/sources.list
RUN apt update &&  apt install -y nginx

ADD ./docker-entrypoint.sh ${JDX_DIR}/docker-entrypoint.sh
ADD ./nginx/jdx-image.conf /etc/nginx/conf.d

ADD ./jdx.jar ${JDX_DIR}/app.jar
ADD ./dist /html
# 入口文件
ENTRYPOINT ["sh", "docker-entrypoint.sh"]
