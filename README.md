# DPTD

## 📋 项目简介

这是一个基于 Spring Boot 的本地生活服务平台（类似大众点评）。本项目涵盖了高并发场景下的常见技术解决方案，旨在打造一个高性能、高可用的后端服务系统。

## 🛠 技术栈

本项目采用主流的 Java 后端技术栈：

- **核心框架**: Spring Boot 2.3.12.RELEASE
- **持久层**: MyBatis Plus 3.4.3
- **数据库**: MySQL 8.0
- **缓存/中间件**: 
  - **Redis** (分布式缓存)
  - **Caffeine** (JVM 进程本地缓存)
  - **Nginx** (反向代理与静态资源缓存)
  - **Canal** (MySQL Binlog 数据同步)
  - Redisson (分布式锁)
  - RabbitMQ (异步消息解耦 - 可选)
- **工具库**: Hutool, Lombok

## ✨ 核心功能

1. **多级缓存架构**: 
   - 构建 **Nginx (OpenResty) + Caffeine + Redis** 的多级缓存体系。
   - 请求优先访问 Nginx 本地缓存，其次访问 JVM 进程缓存 (Caffeine)，最后访问 Redis，层层兜底，极大减少对 MySQL 的直接查询。
   - 利用 **Canal** 监听 MySQL Binlog 变更，异步通知各级缓存进行数据更新或失效，确保缓存与数据库的最终一致性。

2. **短信登录**: 基于 Redis 实现共享 Session 登录，解决集群模式下的 Session 共享问题。

3. **商户查询缓存**: 使用 Redis 缓存商户信息，并结合布隆过滤器（可选）解决缓存穿透、缓存击穿、缓存雪崩等问题。

4. **优惠券秒杀**: 
   - 采用 Redis + Lua 脚本实现库存预扣减，解决超卖问题。
   - 使用 Redisson 分布式锁解决一人一单并发安全问题。
   - 异步下单（优化中），提升高并发响应速度。

5. **达人探店**: 实现博文发布、点赞（SortedSet 排行榜）、关注推送（Feed 流）。

6. **附近的商户**: 基于 Redis GEO 数据结构实现附近商户搜索功能。

7. **用户签到**: 基于 Redis BitMap 实现用户签到统计。

8. **UV 统计**: 使用 HyperLogLog 进行海量数据的 UV 统计。

## 🚀 快速开始

### 环境要求
- JDK 1.8+
- Maven 3.6+
- MySQL 5.7+
- Redis 5.0+
- Nginx (可选，用于测试多级缓存)
- Canal (可选，用于测试数据同步)

### 运行步骤

1. **克隆项目**
   ```bash
   git clone <your-repo-url>
   ```

2. **数据库初始化**
   - 创建数据库 `hmdp`
   - 导入 `sql/hmdp.sql` (如果存在)

3. **配置文件**
   - 修改 `src/main/resources/application.yml` (或 `application-dev.yml`)
   - 更新 MySQL 和 Redis 的连接地址、账号密码。
   - 配置 Canal 客户端连接（如需启用数据同步）。

4. **编译与运行**
   ```bash
   mvn clean install
   java -jar target/hm-dianping-1.0.0-SNAPSHOT.jar
   ```

## 📦 项目结构

```
src/main/java/com/hmdp
├── config      # 配置类 (Redis, MVC, Caffeine 等)
├── controller  # 控制层
├── dto         # 数据传输对象
├── entity      # 实体类
├── mapper      # DAO 层
├── service     # 业务逻辑层
└── utils       # 工具类
```

## 👤 作者

- **Maintainer**: Young

---
*本项目仅供学习与研究使用。*
