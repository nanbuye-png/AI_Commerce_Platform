/**
 * API 端到端测试脚本
 * 验证注册 → 登录 → 受保护接口 → Token 验证 → 异常场景
 * 
 * 使用方法: node api-test.js
 */
const http = require('http');

const BASE_URL = 'http://localhost:8080';
const TEST_USER = {
  username: 'test_user001',
  email: 'test001@example.com',
  password: 'Test123456',
  nickname: 'Test User'
};

function request(method, path, data, token) {
  return new Promise((resolve, reject) => {
    const url = new URL(path, BASE_URL);
    const options = {
      hostname: url.hostname,
      port: url.port,
      path: url.pathname,
      method: method,
      headers: {
        'Content-Type': 'application/json',
      },
    };
    if (token) {
      options.headers['Authorization'] = 'Bearer ' + token;
    }
    const req = http.request(options, (res) => {
      let body = '';
      res.on('data', (chunk) => { body += chunk; });
      res.on('end', () => {
        try {
          resolve({ status: res.statusCode, data: JSON.parse(body) });
        } catch {
          resolve({ status: res.statusCode, data: body });
        }
      });
    });
    req.on('error', reject);
    if (data) {
      req.write(JSON.stringify(data));
    }
    req.end();
  });
}

async function runTests() {
  console.log('===== API 端到端测试 =====\n');

  // 1. 注册
  console.log('1. 测试注册...');
  const registerRes = await request('POST', '/api/auth/register', TEST_USER);
  console.log(`   状态码: ${registerRes.status}`);
  console.log(`   响应: ${JSON.stringify(registerRes.data)}`);
  if (registerRes.data.code === 0 || registerRes.status === 200) {
    console.log('   ✅ 注册成功\n');
  } else if (registerRes.data.code === 400 && registerRes.data.message.includes('already exists')) {
    console.log('   ⚠️ 用户已存在，跳过注册\n');
  } else {
    console.log('   ❌ 注册失败\n');
  }

  // 2. 重复注册测试
  console.log('2. 测试重复注册...');
  const duplicateRes = await request('POST', '/api/auth/register', TEST_USER);
  console.log(`   状态码: ${duplicateRes.status}`);
  console.log(`   响应: ${JSON.stringify(duplicateRes.data)}`);
  if (duplicateRes.data.code !== 0) {
    console.log('   ✅ 重复注册被正确拒绝\n');
  } else {
    console.log('   ⚠️ 重复注册返回成功\n');
  }

  // 3. 登录
  console.log('3. 测试登录...');
  const loginRes = await request('POST', '/api/auth/login', {
    account: TEST_USER.username,
    password: TEST_USER.password
  });
  console.log(`   状态码: ${loginRes.status}`);
  console.log(`   响应: ${JSON.stringify(loginRes.data)}`);
  let token = null;
  if (loginRes.data.code === 0 && loginRes.data.data && loginRes.data.data.token) {
    token = loginRes.data.data.token;
    console.log(`   Token: ${token.substring(0, 30)}...`);
    console.log('   ✅ 登录成功，已获取Token\n');
  } else {
    console.log('   ❌ 登录失败\n');
    return;
  }

  // 4. Token验证 - 检查三段式JWT
  console.log('4. 验证Token格式...');
  const parts = token.split('.');
  if (parts.length === 3) {
    console.log(`   ✅ Token是有效的JWT格式（三段式）\n`);
  } else {
    console.log(`   ❌ Token格式不正确 (段数: ${parts.length})\n`);
  }

  // 5. 受保护接口测试（带Token）
  console.log('5. 测试受保护接口（带Token）...');
  const authRes = await request('GET', '/api/test/auth', null, token);
  console.log(`   状态码: ${authRes.status}`);
  console.log(`   响应: ${JSON.stringify(authRes.data)}`);
  if (authRes.data.code === 0) {
    console.log('   ✅ 带Token访问受保护接口成功\n');
  } else {
    console.log('   ❌ 访问失败\n');
  }

  // 6. 受保护接口测试（无Token）
  console.log('6. 测试受保护接口（无Token）...');
  const noAuthRes = await request('GET', '/api/test/auth');
  console.log(`   状态码: ${noAuthRes.status}`);
  console.log(`   响应: ${JSON.stringify(noAuthRes.data)}`);
  if (noAuthRes.status === 403 || noAuthRes.data.code === 401) {
    console.log('   ✅ 无Token访问被正确拒绝\n');
  } else {
    console.log('   ⚠️ 无Token访问结果: ' + noAuthRes.status + '\n');
  }

  // 7. 错误密码测试
  console.log('7. 测试错误密码...');
  const wrongPwdRes = await request('POST', '/api/auth/login', {
    account: TEST_USER.username,
    password: 'wrongpassword'
  });
  console.log(`   状态码: ${wrongPwdRes.status}`);
  console.log(`   响应: ${JSON.stringify(wrongPwdRes.data)}`);
  if (wrongPwdRes.data.code !== 0) {
    console.log('   ✅ 错误密码登录被正确拒绝\n');
  } else {
    console.log('   ❌ 错误密码登录未拒绝\n');
  }

  console.log('===== 测试完成 =====');
}

runTests().catch(console.error);