## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 17, SpringBoot 3.1.5, MyBatis-Plus 3.5.5, JWT |
| 数据库 | MySQL 8.0 |
| AI | 通义千问（出题 + 作文批改）+ 阿里云 Qwen-ASR（发音评测） |
| 前端 | Vue 3.4, Vite 5, Element Plus 2.4, Pinia, ECharts 5 |

## 核心功能

- **AI 自适应练习** — 追踪薄弱知识点，动态调整难度，智能推荐题目
- **AI 作文批改** — 通义千问四维度评分（语法/内容/结构/词汇）+ 润色建议
- **发音评测** — 浏览器录音 → Qwen-ASR 语音转写 → LCS/Levenshtein 算法评分
- **单词学习** — 翻转卡片 + 艾宾浩斯遗忘曲线规划复习，支持 CET4/CET6/IELTS/TOEFL 词库
- **学习统计** — ECharts 趋势图、薄弱知识点分析、连续打卡统计
- **错题本** — 答错题目记录与解析，分页查看

---

## 环境要求

| 软件 | 最低版本 | 说明 |
|------|----------|------|
| JDK | 17+ | 后端运行环境 |
| Maven | 3.8+ | 后端构建工具 |
| MySQL | 8.0+ | 数据库 |
| Node.js | 18+ | 前端运行环境 |
| npm | 9+ | 前端包管理器（随 Node.js 附带） |

---

## 一、初始化数据库

### 1.1 安装并启动 MySQL

确保 MySQL 8.0+ 已安装并运行。以下以本地安装为例：

```bash
# Windows（以管理员身份运行）
net start mysql80

# macOS（Homebrew）
brew services start mysql

# Linux（systemd）
sudo systemctl start mysql
```

### 1.2 执行初始化脚本

项目 `sql/` 目录包含以下脚本，**按顺序执行**：

| 顺序 | 脚本 | 说明 |
|------|------|------|
| 1 | `init.sql` | 创建数据库 `english_platform`、全部表结构 + 初始数据（用户、知识点、题目） |
| 2 | `fill_word_books.sql` | 导入词库数据（CET4/CET6/IELTS/TOEFL 单词） |

```bash
# 进入项目根目录，依次执行
mysql -u root -p < sql/init.sql
mysql -u root -p < sql/fill_word_books.sql
```

> 如需更多数据，可选择性执行 `expand_data.sql`、`fix_and_expand_data.sql`。

### 1.3 验证数据库

```bash
mysql -u root -p -e "USE english_platform; SHOW TABLES;"
```

预期输出应包含以下表：

| 表 | 说明 |
|----|------|
| `user` | 用户表 |
| `study_checkin` | 打卡记录 |
| `word` | 单词词库 |
| `user_word_record` | 用户单词学习记录 |
| `knowledge_point` | 知识点 |
| `question` | 题目 |
| `question_option` | 题目选项 |
| `user_question_record` | 用户答题记录 |
| `user_knowledge_stat` | 用户知识点薄弱统计 |
| `pronunciation_record` | 发音评测记录 |
| `essay` | 作文批改记录 |

---

## 二、配置后端

### 2.1 获取 API Key

| 服务 | 用途 | 获取地址 |
|------|------|----------|
| 通义千问 (DashScope) | AI 出题 + 作文批改 + 语音转写（发音评测） | https://dashscope.console.aliyun.com/apiKey |

### 2.2 配置方式（二选一）

#### 方式一：环境变量（推荐）

复制 `.env.example` 为 `.env`，填入真实值：

```bash
cp .env.example .env
```

编辑 `.env` 文件：

```properties
DB_HOST=localhost
DB_PORT=3306
DB_NAME=english_platform
DB_USERNAME=root
DB_PASSWORD=你的数据库密码

JWT_SECRET=一个至少32位的随机字符串

TONGYI_API_KEY=sk-xxxxxxxxxxxxxxxx
```

> 环境变量通过 IDE（IntelliJ IDEA 的 EnvFile 插件）或命令行 `export` / `set` 注入。Spring Boot 会自动读取。

#### 方式二：直接编辑配置文件

编辑 [application.yml](backend/src/main/resources/application.yml)，将 `${...}` 占位符替换为真实值：

```yaml
spring:
  datasource:
    password: 你的数据库密码

jwt:
  secret: 一个至少32位的随机字符串

tongyi:
  api-key: sk-xxxxxxxxxxxxxxxx
```

### 2.3 各配置项说明

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `DB_HOST` | `localhost` | 数据库地址 |
| `DB_PORT` | `3306` | 数据库端口 |
| `DB_NAME` | `english_platform` | 数据库名称 |
| `DB_USERNAME` | `root` | 数据库用户名 |
| `DB_PASSWORD` | — | **必填**，数据库密码 |
| `JWT_SECRET` | — | **必填**，JWT 签名密钥，至少 32 位随机字符串 |
| `TONGYI_API_KEY` | — | **必填**，通义千问 API Key |

---

## 三、启动后端

### 3.1 使用 Maven（开发模式）

```bash
cd backend

# 安装依赖并启动（默认端口 8080）
mvn spring-boot:run
```

### 3.2 使用 IDE

在 IntelliJ IDEA 中：
1. `File` → `Open` → 选择 `backend/` 目录
2. 等待 Maven 依赖下载完成
3. 右键 `EnglishLearningPlatformApplication.java` → `Run`

### 3.3 验证后端

浏览器访问：http://localhost:8080/

若看到 Spring Boot 默认页面或无报错响应，说明后端启动成功。

---

## 四、启动前端

### 4.1 安装依赖

```bash
cd frontend
npm install
```

### 4.2 开发模式启动

```bash
npm run dev
```

启动后浏览器访问：**http://localhost:5173**

### 4.3 构建生产版本（可选）

```bash
npm run build      # 输出到 frontend/dist/
npm run preview    # 本地预览生产版本
```

---

## 五、默认账号

数据库初始化后会创建以下测试账号（BCrypt 加密存储）：

| 用户名 | 密码 | 角色 |
|--------|------|------|
| `admin` | `admin123` | 管理员（高级） |
| `test` | `admin123` | 测试用户（初级） |

> 如需修改密码，可通过注册新账号或调用后端接口修改。

---

## 六、项目结构

```
EN/
├── backend/                          # 后端 SpringBoot 项目
│   ├── pom.xml                       # Maven 依赖配置
│   └── src/main/
│       ├── java/com/english/platform/
│       │   ├── EnglishLearningPlatformApplication.java   # 启动入口
│       │   ├── ai/                   # AI 客户端（通义千问）
│       │   ├── algorithm/            # 自适应推荐算法
│       │   ├── config/               # Spring 配置（Security、MyBatis-Plus、跨域）
│       │   ├── controller/           # REST 接口层
│       │   ├── dto/                  # 数据传输对象
│       │   ├── entity/               # 数据库实体
│       │   ├── mapper/               # MyBatis Mapper
│       │   ├── service/              # 业务逻辑层
│       │   ├── util/                 # 工具类（JWT 等）
│       │   └── vo/                   # 视图对象
│       └── resources/
│           ├── application.yml       # 应用配置
│           └── mapper/               # MyBatis XML 映射文件
├── frontend/                         # 前端 Vue 3 项目
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── main.js                   # 入口
│       ├── api/                      # API 请求封装
│       ├── router/                   # 路由配置
│       ├── store/                    # Pinia 状态管理
│       ├── views/                    # 页面组件
│       │   ├── Home.vue              # 首页仪表盘
│       │   ├── Practice.vue          # AI 自适应练习
│       │   ├── AIEssay.vue           # AI 作文批改
│       │   ├── Pronunciation.vue     # 发音评测
│       │   ├── WordStudy.vue         # 单词学习
│       │   ├── Statistics.vue        # 学习统计
│       │   ├── WrongBook.vue         # 错题本
│       │   ├── Profile.vue           # 个人中心
│       │   ├── Login.vue             # 登录
│       │   └── Register.vue          # 注册
│       └── layouts/                  # 布局组件
├── sql/                              # 数据库脚本
│   ├── init.sql                      # 建库建表 + 初始数据
│   └── fill_word_books.sql           # 词库导入
├── .env.example                      # 环境变量配置模板
└── .gitignore
```

---

## 七、自适应推荐算法

```
答题 → 更新薄弱分数 → 查询最薄弱知识点
     → 动态调整难度（正确率 <40% 降 / >70% 升）
     → 排除近期已答题目 → 返回推荐题目
```

---

## 八、常见问题

### Q1: 启动后端报 `Communications link failure`

MySQL 未启动或连接信息配置错误。检查：
- MySQL 服务是否运行
- `DB_HOST` / `DB_PORT` / `DB_USERNAME` / `DB_PASSWORD` 是否正确

### Q2: 启动后端报 `Unknown database 'english_platform'`

未执行数据库初始化脚本，执行：
```bash
mysql -u root -p < sql/init.sql
```

### Q3: AI 功能无响应 / 报错

通义千问 API Key 未配置或无效：
- 检查 `.env` 或 `application.yml` 中 `TONGYI_API_KEY` 的值
- 确认 DashScope 账户余额充足
- 查看后端控制台错误日志

### Q4: 前端访问后端接口 404 / CORS 错误

- 确保后端已启动（`http://localhost:8080`）
- 检查前端 Vite 代理配置（`vite.config.js` 中 `server.proxy` 指向 `http://localhost:8080`）

### Q5: 前端 `npm install` 报错

- 检查 Node.js 版本（需 18+）：`node -v`
- 尝试清除 npm 缓存：`npm cache clean --force`
- 删除 `node_modules/` 和 `package-lock.json` 后重试
