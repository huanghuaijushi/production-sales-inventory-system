# 阿里云 ECS 部署实录

本文记录一次从登录服务器到部署成功的完整过程，包含步骤、命令、遇到的问题和处理方式。文档中的服务器地址、仓库地址、本地私钥路径和部署路径均使用占位符，实际值只在运维人员本地保存。

## 1. 服务器信息

```text
公网 IP：<SERVER_HOST>
系统：Alibaba Cloud Linux 3.2104 LTS 64 位
部署分支：main
项目目录：<APP_DIR>
前端发布目录：<WEB_ROOT>
后端服务名：psi-api
```

## 2. SSH 登录

开始时尝试密码登录，但服务器返回：

```text
Permission denied (publickey,gssapi-keyex,gssapi-with-mic)
```

说明该服务器 SSH 禁用了密码登录，或当前 SSH 配置只允许密钥登录。

随后在阿里云创建密钥对，并把私钥下载到本机：

```text
<SSH_KEY_PATH>
```

先修正私钥权限：

```bash
chmod 600 <SSH_KEY_PATH>
```

验证 SSH 登录：

```bash
ssh -i <SSH_KEY_PATH> <SSH_USER>@<SERVER_HOST> 'uname -a && cat /etc/os-release | head -n 5'
```

确认可以登录后，后续所有服务器操作都通过这个密钥执行。

## 3. 检查服务器环境

检查服务器已有命令：

```bash
ssh -i <SSH_KEY_PATH> <SSH_USER>@<SERVER_HOST> \
  'which dnf yum java mvn node npm nginx mysql redis-server git || true'
```

结果：

```text
dnf / yum 可用
java / mvn / node / npm / nginx / mysql / redis-server / git 缺失或未配置
```

项目需要：

```text
Java 21
Maven 3.6.3+
Node.js 20.19+
npm
MySQL 8
Redis
Nginx
Git
```

## 4. 安装基础环境

安装运行环境：

```bash
ssh -i <SSH_KEY_PATH> <SSH_USER>@<SERVER_HOST> \
  'dnf install -y java-21-openjdk-devel maven git nginx mysql-server redis nodejs npm'
```

安装完成后检查版本：

```bash
ssh -i <SSH_KEY_PATH> <SSH_USER>@<SERVER_HOST> \
  'java -version; mvn -version | head -n 3; node -v; npm -v; nginx -v; mysql --version; redis-server --version'
```

遇到第一个问题：系统默认 Java 指向 Java 11。

输出显示：

```text
openjdk version "11.0.25"
Maven 使用 Java 11
```

处理方式：查看 Java alternatives，再切换到 Java 21。

```bash
ssh -i <SSH_KEY_PATH> <SSH_USER>@<SERVER_HOST> \
  'alternatives --display java | sed -n "1,120p"; alternatives --display javac | sed -n "1,120p"'
```

切换命令：

```bash
ssh -i <SSH_KEY_PATH> <SSH_USER>@<SERVER_HOST> \
  'alternatives --set java /usr/lib/jvm/java-21-alibaba-dragonwell-21.0.10.0.10-1.1.al8.x86_64/bin/java && \
   alternatives --set javac /usr/lib/jvm/java-21-alibaba-dragonwell-21.0.10.0.10-1.1.al8.x86_64/bin/javac && \
   java -version && javac -version'
```

确认结果：

```text
openjdk version "21"
javac 21
```

## 5. 启动 MySQL、Redis、Nginx

```bash
ssh -i <SSH_KEY_PATH> <SSH_USER>@<SERVER_HOST> \
  'systemctl enable --now mysqld redis nginx && systemctl is-active mysqld redis nginx'
```

结果：

```text
active
active
active
```

## 6. 创建项目目录和数据库

创建目录：

```bash
mkdir -p <APP_CONFIG_DIR> <APP_PARENT_DIR> <WEB_ROOT>
chmod 700 <APP_CONFIG_DIR>
```

生成数据库密码和 JWT 密钥，并创建数据库账号。

实际命令结构如下，密码和密钥不写入文档：

```bash
DBPASS="<随机数据库密码>"
JWTPASS="<随机JWT密钥>"

printf "DB_PASSWORD=%s\nJWT_SECRET=%s\n" "$DBPASS" "$JWTPASS" > /etc/psi/secrets.tmp
chmod 600 /etc/psi/secrets.tmp

mysql -uroot -e "
CREATE DATABASE IF NOT EXISTS production_sales_inventory
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '<DB_USER>'@'localhost' IDENTIFIED BY '$DBPASS';
ALTER USER '<DB_USER>'@'localhost' IDENTIFIED BY '$DBPASS';
GRANT ALL PRIVILEGES ON <DB_NAME>.* TO '<DB_USER>'@'localhost';
FLUSH PRIVILEGES;
"
```

## 7. 拉取正式分支代码

使用 GitHub 上的正式分支 `main`：

```bash
ssh -i <SSH_KEY_PATH> <SSH_USER>@<SERVER_HOST> \
  'if [ -d <APP_DIR>/.git ]; then
     cd <APP_DIR> && git fetch origin main && git checkout main && git reset --hard origin/main;
   else
     git clone --branch main <REPO_URL> <APP_DIR>;
   fi && \
   cd <APP_DIR> && git rev-parse --abbrev-ref HEAD && git rev-parse --short HEAD'
```

当时部署到的提交：

```text
main
<COMMIT_SHA>
```

## 8. 构建后端

第一次构建命令：

```bash
ssh -i <SSH_KEY_PATH> <SSH_USER>@<SERVER_HOST> \
  'cd <APP_DIR>/production-sales-inventory-api && \
   JAVA_HOME=<JAVA_HOME> \
   mvn -q -DskipTests package'
```

遇到第二个问题：系统 Maven 太旧。

错误：

```text
maven-compiler-plugin:3.13.0 requires Maven version 3.6.3
```

系统源安装的是 Maven 3.6.2，所以需要手动安装新版 Maven。

第一次尝试下载 Maven 3.9.11：

```bash
curl -fL -o apache-maven-3.9.11-bin.tar.gz \
  https://dlcdn.apache.org/maven/maven-3/3.9.11/binaries/apache-maven-3.9.11-bin.tar.gz
```

遇到 404，说明该版本地址不可用。

改用 Apache archive 下载 Maven 3.9.10：

```bash
ssh -i <SSH_KEY_PATH> <SSH_USER>@<SERVER_HOST> \
  'cd /tmp && \
   curl -fL -o apache-maven-3.9.10-bin.tar.gz \
     https://archive.apache.org/dist/maven/maven-3/3.9.10/binaries/apache-maven-3.9.10-bin.tar.gz && \
   tar -xzf apache-maven-3.9.10-bin.tar.gz && \
   rm -rf /opt/maven && \
   mv apache-maven-3.9.10 /opt/maven && \
   /opt/maven/bin/mvn -version | head -n 2'
```

确认：

```text
Apache Maven 3.9.10
Maven home: /opt/maven
```

然后重新构建后端：

```bash
ssh -i <SSH_KEY_PATH> <SSH_USER>@<SERVER_HOST> \
  'cd <APP_DIR>/production-sales-inventory-api && \
   JAVA_HOME=<JAVA_HOME> \
   <MAVEN_BIN> -q -DskipTests package'
```

确认 jar 文件：

```bash
ssh -i <SSH_KEY_PATH> <SSH_USER>@<SERVER_HOST> \
  'ls -1 <APP_DIR>/production-sales-inventory-api/target/*.jar'
```

结果：

```text
<APP_DIR>/production-sales-inventory-api/target/production-sales-inventory-api-0.0.1-SNAPSHOT.jar
```

## 9. 配置后端 systemd 服务

写入后端环境变量：

```bash
cat > <APP_ENV_FILE> <<EOF
SERVER_ADDRESS=127.0.0.1
SERVER_PORT=8080
WEB_ORIGIN=http://<SERVER_HOST>
DB_URL=jdbc:mysql://localhost:3306/production_sales_inventory?useUnicode=true\&characterEncoding=utf8\&serverTimezone=UTC\&allowPublicKeyRetrieval=true\&useSSL=false
DB_USERNAME=<DB_USER>
DB_PASSWORD=<数据库密码>
REDIS_HOST=localhost
REDIS_PORT=6379
JWT_SECRET=<JWT密钥>
JAVA_HOME=<JAVA_HOME>
EOF

chmod 600 <APP_ENV_FILE>
```

写入 systemd 服务：

```bash
cat > /etc/systemd/system/psi-api.service <<EOF
[Unit]
Description=Production Sales Inventory API
After=network.target mysqld.service redis.service
Wants=mysqld.service redis.service

[Service]
Type=simple
WorkingDirectory=<APP_DIR>/production-sales-inventory-api
EnvironmentFile=<APP_ENV_FILE>
ExecStart=/usr/bin/java -jar <APP_DIR>/production-sales-inventory-api/target/production-sales-inventory-api-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=5
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
EOF
```

启动后端：

```bash
systemctl daemon-reload
systemctl enable --now psi-api
systemctl is-active psi-api
```

## 10. 验证后端

最开始健康检查请求失败：

```bash
curl -s http://127.0.0.1:8080/actuator/health
```

当时原因不是服务失败，而是 Spring Boot 还在启动和执行 Flyway。

查看日志：

```bash
systemctl status psi-api --no-pager -l
journalctl -u psi-api -n 120 --no-pager
```

日志显示：

```text
Successfully applied 1 migration to schema `production_sales_inventory`, now at version v1
Tomcat started on port 8080
Started ProductionSalesInventoryApiApplication
```

稍等后重新验证：

```bash
curl -s -i http://127.0.0.1:8080/actuator/health
```

结果：

```json
{"status":"UP","groups":["liveness","readiness"]}
```

## 11. 构建前端

前端默认 API 地址是：

```text
/api/v1
```

所以线上不用改前端配置，由 Nginx 负责把 `/api/` 转发给后端。

构建和发布命令：

```bash
ssh -i <SSH_KEY_PATH> <SSH_USER>@<SERVER_HOST> \
  'cd <APP_DIR>/production-sales-inventory-web && \
   npm ci && \
   npm run build && \
   rm -rf <WEB_ROOT>/* && \
   cp -a dist/. <WEB_ROOT>/'
```

构建成功后输出了一个提示：

```text
Some chunks are larger than 500 kB after minification
```

这是 Vite 打包体积提示，不影响部署成功。

## 12. 配置 Nginx

写入站点配置：

```bash
cat > /etc/nginx/conf.d/psi.conf <<EOF
server {
    listen 80;
    server_name <SERVER_HOST>;

    root <WEB_ROOT>;
    index index.html;

    client_max_body_size 20m;

    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    location /actuator/ {
        proxy_pass http://127.0.0.1:8080/actuator/;
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    location / {
        try_files \$uri \$uri/ /index.html;
    }
}
EOF
```

检查并重载 Nginx：

```bash
nginx -t
systemctl reload nginx
```

遇到第三个问题：服务器本机直接访问 `127.0.0.1` 时命中 Nginx 默认页面。

命令：

```bash
curl -s -i http://127.0.0.1/ | sed -n "1,10p"
```

原因：

```text
Nginx 默认 server_name 是 _
项目配置的 server_name 是 <SERVER_HOST>
本机请求 127.0.0.1 时 Host 不匹配，所以命中默认站点
```

验证项目站点需要带 Host：

```bash
curl -s -i -H "Host: <SERVER_HOST>" http://127.0.0.1/ | sed -n "1,14p"
curl -s -i -H "Host: <SERVER_HOST>" http://127.0.0.1/actuator/health | sed -n "1,18p"
```

结果：

```text
前端页面：200 OK
后端代理：200 OK
```

从本机外部访问公网 IP：

```bash
curl -s -I --connect-timeout 5 http://<SERVER_HOST>/
```

结果：

```text
HTTP/1.1 200 OK
```

## 13. 管理员账号处理

线上库由 Flyway V1 初始化，默认存在用户：

```text
admin
```

但默认密码验证失败：

```bash
curl -s -X POST http://127.0.0.1:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

返回：

```text
Invalid username or password
```

处理方式：生成一个临时 BCrypt 密码哈希，写入 `sys_user.password_hash`。

生成密码和哈希的本机命令：

```bash
PASS="<临时密码>"
HASH="$(htpasswd -bnBC 10 "" "$PASS" | sed 's/^://')"
```

注意：BCrypt 哈希里有 `$`，直接拼进远程 shell 容易被展开破坏。所以实际写入前先把哈希 base64，再在服务器上解码写入。

写入结构：

```bash
HASH="$(printf %s '<base64后的哈希>' | base64 -d)"
mysql -uroot production_sales_inventory -e "
UPDATE sys_user
SET password_hash = '${HASH}',
    token_version = token_version + 1
WHERE username = 'admin';
SELECT LENGTH(password_hash)
FROM sys_user
WHERE username = 'admin';
"
```

验证登录成功：

```bash
curl -s -X POST http://127.0.0.1:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"<临时密码>"}'
```

返回：

```text
"code":200
```

## 14. 最终验证

服务状态：

```bash
ssh -i <SSH_KEY_PATH> <SSH_USER>@<SERVER_HOST> \
  'systemctl is-active psi-api nginx mysqld redis'
```

结果：

```text
active
active
active
active
```

数据库迁移：

```bash
mysql -uroot production_sales_inventory -e "
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
"
```

结果：

```text
installed_rank  version  description  success
1               1        init schema  1
```

公网前端：

```bash
curl -s -I --connect-timeout 5 http://<SERVER_HOST>/
```

结果：

```text
HTTP/1.1 200 OK
```

## 15. 后续重复部署命令

以后更新正式环境，一般执行：

```bash
cd <APP_DIR>
git fetch origin main
git checkout main
git reset --hard origin/main

cd <APP_DIR>/production-sales-inventory-api
JAVA_HOME=<JAVA_HOME> <MAVEN_BIN> -q -DskipTests package
systemctl restart psi-api

cd <APP_DIR>/production-sales-inventory-web
npm ci
npm run build
rm -rf <WEB_ROOT>/*
cp -a dist/. <WEB_ROOT>/
systemctl reload nginx
```

验证：

```bash
systemctl status psi-api --no-pager
systemctl status nginx --no-pager
curl -I http://<SERVER_HOST>/
curl http://127.0.0.1:8080/actuator/health
```

## 16. 本次结论

本次部署成功后：

```text
Web 地址：http://<SERVER_HOST>
Nginx：负责前端静态文件和 /api/ 反向代理
Spring Boot：由 systemd 的 psi-api 服务托管
MySQL：本机 production_sales_inventory
Redis：本机 6379
```

生产注意事项：

- 不开放 3306、6379、8080 到公网。
- SSH 22 端口建议限制为自己的公网 IP。
- 登录后尽快修改管理员临时密码。
- 后续绑定域名后配置 HTTPS。
- 正式数据上线前配置数据库定时备份。
