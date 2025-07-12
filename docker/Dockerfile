FROM openjdk:11.0.16-jdk
# envs
ENV JDX_DIR=/jdx

# 工作目录
WORKDIR ${JDX_DIR}

RUN apt update &&  apt install -y nginx

ADD ./docker-entrypoint.sh ${JDX_DIR}/docker-entrypoint.sh
ADD ./nginx/jdx.conf /etc/nginx/conf.d

ADD ./jdx.jar ${JDX_DIR}/app.jar
ADD ./dist /html

EXPOSE 6443
EXPOSE 8090

# 健康检查
HEALTHCHECK --interval=5s --timeout=3s CMD curl -f http://localhost:8090/actuator/health || exit 1


# 入口文件
ENTRYPOINT ["sh", "docker-entrypoint.sh"]
