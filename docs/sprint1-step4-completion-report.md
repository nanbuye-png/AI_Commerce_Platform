# Sprint 1 Step 4 Completion Report

## 用户注册/登录接口实现

---

## 1. 新增文件列表

| # | 文件路径 | 说明 |
|---|---------|------|
| 1 | `backend/commerce-platform/src/main/java/com/commerce/platform/user/dto/RegisterRequest.java` | 注册请求 DTO（username + email + password + nickname + phone，含 @NotBlank/@Email 校验） |
| 2 | `backend/commerce-platform/src/main/java/com/commerce/platform/user/dto/LoginRequest.java` | 登录请求 DTO（account 支持 username 或 email + password） |
| 3 | `backend/commerce-platform/src/main/java/com/commerce/platform/user/dto/AuthResponse.java` | 认证响应 DTO（token + userId + username + role） |
| 4 | `backend/commerce-platform/src/main/java/com/commerce/platform/user/dto/UserResponse.java` | 用户信息响应 DTO（不含 passwordHash，通过 `UserResponse.from(User)` 工厂方法构建） |
| 5 | `backend/commerce-platform/src/main/java/com/commerce/platform/user/service/UserService.java` | 用户服务层（注册/登录/查询核心逻辑） |
| 6 | `backend/commerce-platform/src/main/java/com/commerce/platform/user/controller/AuthController.java` | 认证控制器（POST /api/auth/register 和 POST /api/auth/login） |
| 7 | `backend/commerce-platform/src/test/java/com/commerce/platform/user/controller/AuthControllerTest.java` | AuthController 集成测试（6 个测试用例） |

## 2. 修改文件列表

| # | 文件路径 | 变更内容 |
|---|---------|---------|
| 1 | `backend/commerce-platform/src/main/java/com/commerce/platform/common/config/SecurityConfig.java` | 放行 `/api/auth/register`、`/api/auth/login`、`/api/health`，其余接口保持认证要求 |

## 3. 注册流程说明

```
POST /api/auth/register
  │
  ▼
AuthController.register(@Valid RegisterRequest)
  │
  ▼
UserService.register(RegisterRequest)
  │
  ├── 1. 参数校验 (@NotBlank/@Email → 由 @Valid 触发)
  ├── 2. 检查 username 是否已存在 → 存在则返回 Result.error(400)
  ├── 3. 检查 email 是否已存在 → 存在则返回 Result.error(400)
  ├── 4. BCryptPasswordEncoder.encode(password)
  ├── 5. 创建 User 实体（role=CUSTOMER, status=ACTIVE）
  ├── 6. userRepository.save(user)
  ├── 7. UserResponse.from(savedUser) — 构建 DTO（不含 passwordHash）
  └── 8. 返回 Result.success(userResponse)
```

**关键设计决策：**
- 引入 `UserResponse` DTO 替代直接返回 `User` 实体。避免调用 `savedUser.setPasswordHash(null)` 触发 JPA dirty checking 将 null 写入 NOT NULL 列（`password_hash`），从而消除 500 错误。
- 通过 `UserResponse.from(User)` 工厂方法安全映射，`passwordHash` 字段永不进入响应。

## 4. 登录流程说明

```
POST /api/auth/login
  │
  ▼
AuthController.login(@Valid LoginRequest)
  │
  ▼
UserService.login(LoginRequest)
  │
  ├── 1. 根据 account 查询用户（先按 username，再按 email）
  ├── 2. 用户不存在 → Result.error(400, "account or password is incorrect")
  ├── 3. 检查 user.status == ACTIVE → 否则 Result.error(400, "account is disabled")
  ├── 4. BCryptPasswordEncoder.matches(password, user.passwordHash) → 不匹配则 Result.error(400)
  ├── 5. JwtUtil.generateToken(userId, username, role)
  ├── 6. 构建 AuthResponse(token + userId + username + role)
  └── 7. 返回 Result.success(authResponse)
```

## 5. JWT 返回结构

### 登录成功响应示例

```json
{
  "code": 0,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJ0ZXN0X3VzZXIiLCJyb2xlIjoiQ1VTVE9NRVIiLCJpYXQiOjE3NTM0Mzk0MDAsImV4cCI6MTc1MzUyNTgwMH0.xxx",
    "userId": 1,
    "username": "test_user",
    "role": "CUSTOMER"
  }
}
```

### 注册成功响应示例

```json
{
  "code": 0,
  "data": {
    "id": 1,
    "username": "test_user",
    "email": "test@example.com",
    "nickname": "Test User",
    "phone": null,
    "avatar": null,
    "role": "CUSTOMER",
    "status": "ACTIVE",
    "createdTime": "2026-07-25T16:30:00",
    "updatedTime": "2026-07-25T16:30:00"
  }
}
```

### 错误响应示例

```json
{
  "code": 400,
  "message": "username already exists"
}
```

> 注：`data` 字段为 null 时，因 `Result` 类标注 `@JsonInclude(NON_NULL)`，该字段会从 JSON 中省略。

## 6. 数据库变化

| 变更类型 | 说明 |
|---------|------|
| **新增记录** | 注册成功后 `users` 表新增一条记录，password_hash 存储 BCrypt 加密值 |
| **Schema 不变** | 无 DDL 变更，使用已有的 `users` 表结构 |

## 7. 测试结果

### mvn test — BUILD SUCCESS

```
Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
```

| 测试类 | 测试方法 | 结果 |
|-------|---------|------|
| AuthControllerTest | shouldRegisterSuccessfully | ✅ 通过 |
| AuthControllerTest | shouldLoginWithUsernameSuccessfully | ✅ 通过 |
| AuthControllerTest | shouldLoginWithEmailSuccessfully | ✅ 通过 |
| AuthControllerTest | shouldFailLoginWithWrongPassword | ✅ 通过 |
| AuthControllerTest | shouldFailRegisterDuplicateUsername | ✅ 通过 |
| AuthControllerTest | shouldFailRegisterDuplicateEmail | ✅ 通过 |
| JwtUtilTest | shouldGenerateTokenSuccessfully | ✅ 通过 |
| JwtUtilTest | shouldParseTokenAndExtractClaims | ✅ 通过 |
| JwtUtilTest | shouldRejectExpiredToken | ✅ 通过 |
| UserRepositoryTest | (已有测试) | ✅ 通过 |

### 测试覆盖场景

| 场景 | 验证点 |
|------|-------|
| 正常注册 | user 写入 DB，password 以 BCrypt 哈希存储，响应不含 passwordHash |
| username 重复注册 | 返回 code=400, message="username already exists" |
| email 重复注册 | 返回 code=400, message="email already exists" |
| username 登录 | 返回 token + userId + username + role（role=CUSTOMER） |
| email 登录 | 返回 token + username 正确 |
| 错误密码登录 | 返回 code=400 |

## 8. 遇到的问题与解决

### 问题 1：注册接口返回 500 错误（NOT NULL 约束冲突）

**根因：** 初始实现中 `UserService.register()` 在 `userRepository.save()` 后调用 `savedUser.setPasswordHash(null)` 以清除返回给客户端的密码。但 `savedUser` 仍是 JPA 托管实体，`setPasswordHash(null)` 触发 dirty checking，事务提交时 Hibernate 将 null 写入数据库 `password_hash` 列（NOT NULL 约束），抛出 `DataIntegrityViolationException`。

**解决方案：** 
1. 新建 `UserResponse` DTO，字段包含 User 全部可公开信息但不含 `passwordHash`
2. `UserService.register()` 改为通过 `UserResponse.from(savedUser)` 构建响应
3. 不再修改托管实体的任何字段

**影响范围：** `AuthController.register()` 返回类型从 `Result<User>` 改为 `Result<UserResponse>`。

### 问题 2：测试断言 `$.data.isEmpty()` 失败（PathNotFoundException）

**根因：** 错误响应中 `Result.data = null`，因 `@JsonInclude(NON_NULL)` 省略该字段，JSON 中不存在 `data` 键，`isEmpty()` 要求路径存在但值为空，导致 `PathNotFoundException`。

**解决方案：** 将 `shouldFailLoginWithWrongPassword` 中的 `jsonPath("$.data").isEmpty()` 改为 `jsonPath("$.data").doesNotExist()`。

---

## 无修改的现有组件（任务约束）

| 组件 | 状态 |
|------|------|
| User Entity | ✅ 未修改 |
| UserRepository | ✅ 未修改 |
| JwtUtil | ✅ 未修改 |
| JwtAuthenticationFilter | ✅ 未修改 |
| SecurityConfig 基础配置 | ✅ 仅新增放行路径 |
| PasswordEncoder Bean | ✅ 未修改 |

---

## Security 放行配置（最终状态）

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/register", "/api/auth/login", "/api/health").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    return http.build();
}
```

---

**完成时间：** 2026-07-25 16:43 UTC+8

**状态：** ✅ Sprint 1 Step 4 完成