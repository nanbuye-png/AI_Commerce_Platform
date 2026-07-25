# Sprint 1 Step 3 Completion Report

## JWT 认证体系设计与实现

---

## 1. 新增文件列表

| # | 文件路径 | 说明 |
|---|---------|------|
| 1 | `backend/commerce-platform/src/main/java/com/commerce/platform/common/security/JwtProperties.java` | JWT 配置属性类（绑定 application.yml） |
| 2 | `backend/commerce-platform/src/main/java/com/commerce/platform/common/security/JwtUtil.java` | JWT 工具类（生成/解析/验证 Token） |
| 3 | `backend/commerce-platform/src/main/java/com/commerce/platform/common/security/JwtAuthenticationFilter.java` | JWT 认证过滤器（OncePerRequestFilter） |
| 4 | `backend/commerce-platform/src/test/java/com/commerce/platform/common/security/JwtUtilTest.java` | JwtUtil 单元测试（3 个测试用例） |

## 2. 修改文件列表

| # | 文件路径 | 变更内容 |
|---|---------|---------|
| 1 | `backend/commerce-platform/pom.xml` | 新增 jjwt-api / jjwt-impl / jjwt-jackson 依赖（0.12.5） |
| 2 | `backend/commerce-platform/src/main/resources/application.yml` | 新增 jwt.secret 和 jwt.expiration 配置项 |
| 3 | `backend/commerce-platform/src/main/java/com/commerce/platform/common/config/SecurityConfig.java` | 添加 JwtAuthenticationFilter、PasswordEncoder Bean，配置放行 /api/health |

## 3. JWT 架构说明

```
请求流程：

Client
  │
  ▼
Authorization Header: Bearer <token>
  │
  ▼
JwtAuthenticationFilter (OncePerRequestFilter)
  │
  ├── 读取 Authorization Header
  ├── 提取 Bearer Token
  ├── JwtUtil.validateToken() → 验证签名 + 过期时间
  ├── JwtUtil.parseToken() → 解析 Claims
  ├── 构建 UsernamePasswordAuthenticationToken
  └── 设置 SecurityContextHolder
  │
  ▼
SecurityFilterChain.authorizeHttpRequests
  │
  └── /api/health → permitAll
  └── 其他 → authenticated
```

**组件关系：**

```
JwtProperties (配置源)
     │
     ▼
JwtUtil (Token 能力)
     │
     ▼
JwtAuthenticationFilter (认证过滤器)
     │
     ▼
SecurityConfig (Spring Security 集成)
```

## 4. Security 配置说明

| 配置项 | 设置 | 说明 |
|-------|------|------|
| CSRF | 关闭 | RESTful API + JWT 无状态，不依赖 Cookie |
| Session | STATELESS | 无状态会话管理 |
| 放行路径 | `/api/health` | 健康检查接口无需认证 |
| 其他路径 | 需要认证 | 通过 JWT Token 认证 |
| 过滤器顺序 | Before UsernamePasswordAuthenticationFilter | JWT 认证优先于表单登录 |
| 密码编码器 | BCryptPasswordEncoder | 为后续登录注册准备 |

## 5. Token 结构说明

### JWT Payload

```json
{
  "sub": "1",           // userId (String)
  "username": "testuser",
  "role": "CUSTOMER",   // CUSTOMER | MERCHANT | ADMIN | SUPER_ADMIN
  "iat": 1752148800,    // 签发时间
  "exp": 1752235200     // 过期时间
}
```

### 生成方法签名

```java
String generateToken(Long userId, String username, String role)
```

### 配置参数

```yaml
jwt:
  secret: ai-commerce-platform-secret-key-change-in-production
  expiration: 86400000  # 24小时（毫秒）
```

## 6. 测试结果

### 单元测试（mvn clean test）

```
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| 测试类 | 测试方法 | 结果 |
|-------|---------|------|
| JwtUtilTest | shouldGenerateTokenSuccessfully | ✅ 通过 |
| JwtUtilTest | shouldParseTokenAndExtractClaims | ✅ 通过 |
| JwtUtilTest | shouldRejectExpiredToken | ✅ 通过 |
| UserRepositoryTest | (已有测试) | ✅ 通过 |

### 集成验证

| 端点 | HTTP 状态 | 结果 |
|-----|----------|------|
| GET /api/health | 200 | ✅ permitAll 生效 |
| GET /api/anything-else | 403 | ✅ 未认证请求被拒绝 |

### Spring Boot 启动日志关键行

```
Will secure any request with [
  ...,
  com.commerce.platform.common.security.JwtAuthenticationFilter@4debbf0,
  ...
]
```

确认 `JwtAuthenticationFilter` 已成功注册到过滤器链中。

## 7. 遇到的问题

### 问题 1：JwtAuthenticationFilter 双重注册风险

**描述：** 初始设计将 `JwtAuthenticationFilter` 标注 `@Component`，Spring Boot 会自动将其注册为全局 Filter，同时 `SecurityConfig` 中通过 `addFilterBefore` 手动添加，导致过滤器执行两次。

**解决方案：** 移除 `@Component` 注解，改为在 `SecurityConfig` 中通过 `@Bean` 方法创建实例，确保仅在 SecurityFilterChain 中注册一次。

### 问题 2：Windows PowerShell 命令链接符

**描述：** PowerShell 不支持 `&&` 命令链接符（需要 cmd /c 包装）。

**解决方案：** 使用 `cmd /c "cd /d ... && mvn ..."` 格式执行复合命令，使用 `curl.exe` 而非 `curl` 避免别名冲突。

---

**完成时间：** 2026-07-25 16:27 UTC+8

**状态：** ✅ Sprint 1 Step 3 完成