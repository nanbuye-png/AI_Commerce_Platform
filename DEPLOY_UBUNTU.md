# Ubuntu 24.04 公网展示部署与日常操作

当前服务器：`47.80.61.59`，项目目录：`/opt/ai-commerce-platform`。所有命令都在阿里云 Workbench 的服务器终端执行；需要以 `root` 登录，或在 Docker 命令前加 `sudo`。

## 访问地址

| 页面 | 地址 |
| --- | --- |
| 客户端 | http://47.80.61.59:5173/ |
| 商家端 | http://47.80.61.59:5174/ |
| 管理端 | http://47.80.61.59:5175/ |

目前使用公网 IP 和 HTTP。请只放测试数据，勿使用真实客户资料、真实支付信息或其他网站正在使用的密码。AI 服务已切换到 `openai-compatible` 模式；模型调用会产生服务商费用。

## 首次部署配置

服务器使用 `docker-compose.yml` 加 `docker-compose.server.yml` 两个文件。后者覆盖演示账号、数据库密码、令牌和跨域设置。不要只运行默认 Compose 文件，否则会启用演示配置。

首次部署时在项目目录运行 `bash scripts/setup-server-env.sh`，生成 `.env`。脚本拒绝覆盖已有文件；`.env` 权限为 `600`，不能提交到 Git。管理员用户名是 `admin`。在自己的服务器终端查看密码，不要粘贴到聊天中：

```bash
cd /opt/ai-commerce-platform
grep '^ADMIN_PASSWORD=' .env
```

## 查看状态与日志

```bash
cd /opt/ai-commerce-platform
docker compose -f docker-compose.yml -f docker-compose.server.yml ps
docker compose -f docker-compose.yml -f docker-compose.server.yml logs --tail=100 commerce-core
```

只看其他服务的日志时，将 `commerce-core` 替换为 `postgres`、`ai-service`、`customer-web`、`merchant-web` 或 `admin-web`。

## 启动、停止和重启

```bash
cd /opt/ai-commerce-platform
docker compose -f docker-compose.yml -f docker-compose.server.yml up -d --no-build
docker compose -f docker-compose.yml -f docker-compose.server.yml restart
docker compose -f docker-compose.yml -f docker-compose.server.yml down
```

`down` 会停止容器，但保留数据库和上传文件卷。不要运行 `down -v`，否则会删除数据库和上传文件。

## AI 模型配置

模型配置保存在服务器的 `.env` 中。首次生成的 `.env` 默认使用 `mock`；要使用当前的真实服务，请在服务器上设置 `AI_LLM_PROVIDER=openai-compatible`、`AI_LLM_BASE_URL=https://apihub.agnes-ai.com/v1`、`AI_DEFAULT_MODEL=agnes-2.5-flash`，并填写自己的 `AI_LLM_API_KEY`。更换密钥时直接在服务器终端编辑 `.env`，不要把密钥写入 Git 或发到聊天中。保存后运行：

```bash
cd /opt/ai-commerce-platform
docker compose -f docker-compose.yml -f docker-compose.server.yml config -q
docker compose -f docker-compose.yml -f docker-compose.server.yml up -d --no-deps --force-recreate ai-service
```

`config -q` 只检查配置，不会打印密钥。AI 容器的健康状态仅说明服务已启动；实际模型连通性还需发送一条短消息验证。

## 排查无法访问

1. 运行上面的 `ps`，确认 PostgreSQL、AI 服务和后端是 `healthy`，三个前端是 `Up`。
2. 在服务器终端运行 `curl -fsS http://127.0.0.1:8080/actuator/health`；正常结果包含 `"status":"UP"`。
3. 如本机正常而公网打不开，在阿里云轻量应用服务器控制台的防火墙中检查 TCP `5173`、`5174`、`5175` 是否放行。
4. 登录时选择对应的端口。管理员账号只在管理端 `5175` 登录。客户端和商家端需要分别注册对应类型的账号。

## 数据与更新注意事项

数据库保存在 Docker 卷 `ai-commerce-platform_postgres-data`，上传文件保存在 `ai-commerce-platform_uploads-data`。改动或升级前先备份。项目代码和部署配置保存在 GitHub，`.env` 只保存在服务器。更新时在项目目录运行：

```bash
git pull --ff-only
docker compose -f docker-compose.yml -f docker-compose.server.yml build
docker compose -f docker-compose.yml -f docker-compose.server.yml up -d --no-build
```

如果 `git pull --ff-only` 报本地文件已修改或无法快进，先检查 `git status`，不要直接覆盖服务器改动。
