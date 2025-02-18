# 使用 OpenJDK 17（Alpine 版本）作为基础镜像
FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/openjdk:17-jdk

# 设置工作目录
WORKDIR /app

# 设置上海时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 复制 jar 文件到容器
COPY target/EmailLog-0.0.1-SNAPSHOT.jar app.jar

# 暴露端口
EXPOSE 8080 8888
# 运行 Spring Boot 应用
ENTRYPOINT ["java", "-jar", "app.jar"]