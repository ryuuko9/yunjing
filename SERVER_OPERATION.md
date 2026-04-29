# 服务器运行与维护手册

## 1. 当前服务器信息

- 公网 IP：`47.237.211.12`
- 系统：`Ubuntu 22.04.5 LTS`
- 登录用户：`root`
- 主后端域名：`http://yunjingzhilian.asia`
- 主后端 HTTPS：`https://yunjingzhilian.asia`
- `assemble` 域名：`http://assemble.yunjingzhilian.asia`
- `assemble` HTTPS：`https://assemble.yunjingzhilian.asia`

登录命令：

```bash
ssh root@47.237.211.12
```

说明：

- 当前已切换到新服务器。
- 当前 `HTTP` 和 `HTTPS` 双栈并行。
- 当前未做 `HTTP -> HTTPS` 强制跳转。

## 2. 当前部署的两个后端

### 2.1 yunjing-backend

- 服务名：`yunjing-backend`
- 运行目录：`/opt/yunjing-backend/app`
- 程序包：`/opt/yunjing-backend/app/yunjing-backend-0.0.1-SNAPSHOT.jar`
- 数据目录：`/opt/yunjing-backend/storage`
- 本机端口：`8080`
- 对外入口：`http://yunjingzhilian.asia`
- HTTPS 入口：`https://yunjingzhilian.asia`
- 健康检查：`http://yunjingzhilian.asia/actuator/health`
- HTTPS 健康检查：`https://yunjingzhilian.asia/actuator/health`

### 2.2 assemble-server

- 服务名：`assemble-server`
- 运行目录：`/opt/assemble-server/app`
- 程序文件：`/opt/assemble-server/app/server.py`
- 数据目录：`/opt/assemble-server/data`
- 本机端口：`5000`
- 对外入口：`http://assemble.yunjingzhilian.asia`
- HTTPS 入口：`https://assemble.yunjingzhilian.asia`
- 健康检查：`http://assemble.yunjingzhilian.asia/ping`
- HTTPS 健康检查：`https://assemble.yunjingzhilian.asia/ping`

## 3. 启动、停止、重启

启动两个服务：

```bash
systemctl start yunjing-backend
systemctl start assemble-server
```

停止两个服务：

```bash
systemctl stop yunjing-backend
systemctl stop assemble-server
```

重启两个服务：

```bash
systemctl restart yunjing-backend
systemctl restart assemble-server
```

一起重启：

```bash
systemctl restart yunjing-backend assemble-server
```

## 4. 查看服务状态

查看服务状态：

```bash
systemctl status yunjing-backend --no-pager
systemctl status assemble-server --no-pager
systemctl status nginx --no-pager
systemctl status mysql --no-pager
```

查看是否开机自启：

```bash
systemctl is-enabled yunjing-backend
systemctl is-enabled assemble-server
systemctl is-enabled nginx
systemctl is-enabled mysql
```

设置开机自启：

```bash
systemctl enable yunjing-backend
systemctl enable assemble-server
systemctl enable nginx
systemctl enable mysql
```

## 5. 查看日志

查看最近日志：

```bash
journalctl -u yunjing-backend -n 100 --no-pager
journalctl -u assemble-server -n 100 --no-pager
journalctl -u nginx -n 100 --no-pager
journalctl -u mysql -n 100 --no-pager
```

实时跟踪日志：

```bash
journalctl -u yunjing-backend -f
journalctl -u assemble-server -f
```

查看 `nginx` 日志：

```bash
tail -n 100 /var/log/nginx/access.log
tail -n 100 /var/log/nginx/error.log
```

## 6. 健康检查与接口验证

检查主后端：

```bash
curl http://127.0.0.1:8080/actuator/health
curl http://yunjingzhilian.asia/actuator/health
curl https://yunjingzhilian.asia/actuator/health
```

检查 `assemble`：

```bash
curl http://127.0.0.1:5000/ping
curl http://assemble.yunjingzhilian.asia/ping
curl https://assemble.yunjingzhilian.asia/ping
```

检查一个实际业务接口：

```bash
curl "http://127.0.0.1:8080/api/merchant/projects?userId=2"
curl "http://yunjingzhilian.asia/api/merchant/projects?userId=2"
```

如果你只想验证反向代理是否正常：

```bash
curl http://127.0.0.1/actuator/health -H "Host: yunjingzhilian.asia"
curl http://127.0.0.1/ping -H "Host: assemble.yunjingzhilian.asia"
```

## 7. 端口与进程检查

查看监听端口：

```bash
ss -ltnp | egrep ':80 |:5000 |:8080 |:443 ' || true
```

当前端口含义：

- `80`：`nginx` 对外入口
- `8080`：`yunjing-backend` 本机服务端口
- `5000`：`assemble-server` 本机服务端口
- `443`：`HTTPS` 对外入口

## 8. 重要配置文件

`yunjing-backend`：

- 环境变量：`/opt/yunjing-backend/app/.env`
- 程序包：`/opt/yunjing-backend/app/yunjing-backend-0.0.1-SNAPSHOT.jar`
- 服务文件：`/etc/systemd/system/yunjing-backend.service`

`assemble-server`：

- 环境变量：`/opt/assemble-server/app/.env`
- 程序文件：`/opt/assemble-server/app/server.py`
- 服务文件：`/etc/systemd/system/assemble-server.service`

`nginx`：

- 主后端站点：`/etc/nginx/sites-available/yunjing-backend`
- `assemble` 站点：`/etc/nginx/sites-available/assemble-server`
- 启用目录：`/etc/nginx/sites-enabled/`

## 9. 修改配置后的生效方式

修改后端环境变量后：

```bash
systemctl restart yunjing-backend
systemctl restart assemble-server
```

修改 `nginx` 配置后：

```bash
nginx -t
systemctl reload nginx
```

修改 `systemd` 服务文件后：

```bash
systemctl daemon-reload
systemctl restart yunjing-backend
systemctl restart assemble-server
```

## 10. 数据与数据库

MySQL 登录：

```bash
mysql -uroot -p
```

查看数据库：

```bash
mysql -e "SHOW DATABASES;"
mysql -D yunjing -e "SHOW TABLES;"
```

查看关键数据量：

```bash
mysql -D yunjing -e "SELECT COUNT(*) AS buyer_tutorial_count FROM buyer_tutorial; SELECT COUNT(*) AS merchant_project_count FROM merchant_project;"
```

当前数据库名：

- `yunjing`

当前业务数据目录：

- `/opt/yunjing-backend/storage`
- `/opt/assemble-server/data`

## 11. 常见排障

### 11.1 主后端启动失败

先看状态：

```bash
systemctl status yunjing-backend --no-pager
```

再看日志：

```bash
journalctl -u yunjing-backend -n 100 --no-pager
```

如果日志里出现：

```text
Public Key Retrieval is not allowed
```

检查：

`/opt/yunjing-backend/app/.env`

里面的数据库连接串是否包含：

```text
allowPublicKeyRetrieval=true
```

### 11.2 `assemble-server` 启动失败

```bash
systemctl status assemble-server --no-pager
journalctl -u assemble-server -n 100 --no-pager
```

### 11.3 外网访问不通

先检查本机服务是否正常：

```bash
curl http://127.0.0.1:8080/actuator/health
curl http://127.0.0.1:5000/ping
```

再检查 `nginx`：

```bash
nginx -t
systemctl status nginx --no-pager
```

再检查安全组是否放行：

- `80`
- `5000`
- `443`

### 11.4 主后端返回 400

这通常是 `nginx` 反代头有问题。  
重点检查：

`/etc/nginx/sites-available/yunjing-backend`

下面这些行必须是正常变量，不能带反斜杠：

```nginx
proxy_set_header Host $host;
proxy_set_header X-Real-IP $remote_addr;
proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
proxy_set_header X-Forwarded-Proto $scheme;
```

改完后执行：

```bash
nginx -t
systemctl reload nginx
```

## 12. 当前公网可用地址

- 主后端健康检查：
  `http://yunjingzhilian.asia/actuator/health`
- 主后端 HTTPS 健康检查：
  `https://yunjingzhilian.asia/actuator/health`
- `assemble` 健康检查：
  `http://assemble.yunjingzhilian.asia/ping`
- `assemble` HTTPS 健康检查：
  `https://assemble.yunjingzhilian.asia/ping`

## 13. 当前本地仓库配置结论

当前 App 和后端生成链接目前仍按下面入口对齐：

- 主后端：`http://yunjingzhilian.asia`
- `assemble`：`http://assemble.yunjingzhilian.asia`

说明：

- 服务器现在已经支持 `HTTPS`
- 但为了不影响现有客户端，当前代码侧仍保留 `HTTP`
- 以后如果你确认 Unity 和 App 都兼容，再切到 `HTTPS`

如果以后公网 IP 再变化：

- 不改 App 代码
- 不改后端代码
- 只改 DNS 解析记录

## 14. 后续建议

- 给新服务器做快照或备份
- 给 MySQL 做定期备份
- 给 `/opt/yunjing-backend/storage` 和 `/opt/assemble-server/data` 做定期备份
- 评估 Unity 和 App 是否可以切到 `HTTPS`
