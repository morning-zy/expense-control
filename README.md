# 企业费用报销与内控审批系统（expense-control）

> 面向「审计信息化」场景的报销审批系统。重点不是把 CRUD 写全，而是把**内控要求**落到代码里：
> 审批链可追溯、状态流转不可重复、操作只增不删。

## 技术栈

| 层次 | 选型 |
|---|---|
| 运行时 | JDK 21 / Spring Boot 3.5.16 |
| 持久层 | MyBatis-Plus 3.5.17 + 手写 XML（多表分页） |
| 数据库 | MySQL 8.0（InnoDB） |
| 鉴权 | jjwt 0.12.6（HS256）+ Servlet 拦截器，密码 PBKDF2WithHmacSHA256 |
| 其他 | Lombok / Jakarta Validation / Spring AOP |

## 核心实现

**1. 审批链：递归 CTE 一条 SQL 算出来，提交时快照成待办**

审批人由组织树上的 `manager_id` 逐级向上推导，用 MySQL 8 的 `WITH RECURSIVE` 一条 SQL 取完整条链，
并以 `WHERE node_seq < maxDepth` 防止组织架构配成环导致无限递归。

审批链在**提交那一刻**落到 `approval_task`（待办）与 `approval_record`（已办流水），
之后组织架构或上级调整，都不会改写已提交单据的审批路径。

**2. 状态流转：条件 UPDATE + 影响行数，不先查后改**

所有状态流转都写成「把前置状态放进 WHERE，然后看影响几行」：

```sql
UPDATE approval_task
SET status = 'APPROVED', decided_at = NOW()
WHERE form_id = ? AND node_seq = ? AND status = 'PENDING'
```

1 行 = 抢到了继续；0 行 = 已被别人处理，直接拒绝。
这比「先 select 查状态、再 update」安全，后者是典型的 check-then-act 竞态。
**所以这套实现不依赖乐观锁**（项目里的 `OptimisticLockerInnerInterceptor` 只是脚手架，业务未使用）。

**3. 单号生成：序列表 + `LAST_INSERT_ID(expr)` 原子递增**

```
BX + yyyyMM + 4 位流水
INSERT INTO seq_generator (seq_name, current_val) VALUES (?, LAST_INSERT_ID(1))
ON DUPLICATE KEY UPDATE current_val = LAST_INSERT_ID(current_val + 1)
```

`last_insert_id` 是连接级变量，靠 `@Transactional` 保证递增与取值在同一连接。

**4. 操作留痕：AOP 环绕通知，成功才记**

`@OpLog` 注解 + `OperationLogAspect`。用 `@Around` 而不是 `@AfterReturning`：
业务抛异常时事务会回滚，此时写日志会产生「日志说有、库里没有」的假审计记录。
日志写入失败只 `log.warn`，不影响主流程。

**5. 登录鉴权：JWT + 拦截器，身份不再由客户端决定**

`AuthInterceptor` 校验令牌并写入 `UserContext`（ThreadLocal，`afterCompletion` 清理防线程池污染）。
登录失败统一返回「工号或密码错误」，不区分账号不存在与密码错误，避免账号枚举。

## 数据模型

10 张表，见 `docs/sql/schema.sql`。删除语义分三类：

| 类型 | 表 | 策略 |
|---|---|---|
| 基础数据 | 部门 / 员工 / 费用科目 | `status` 停用，不删 |
| 业务单据 | 报销单 / 明细 / 预算 | `deleted` 逻辑删除 |
| 审计记录 | 审批任务 / 审批流水 / 操作日志 | 只增不删 |

`budget` 用 `occupied_amount`（审批中占用）+ `used_amount`（已支付）双字段，
对应「预算占用 → 预算消耗」两阶段。

## 快速开始

```bash
# 1. 建库建表
mysql -u root -p < docs/sql/schema.sql
mysql -u root -p < docs/sql/data.sql      # 初始化数据，所有员工密码 123456

# 2. 配置数据库凭据
cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml

# 3. 打包 & 启动
mvn clean package -DskipTests
bash scripts/start.sh                     # 默认 8081
bash scripts/status.sh
bash scripts/stop.sh
```

启动后访问 `http://localhost:8081/api/ping` 验证。

详细部署步骤见 `docs/部署说明.md`。

## 接口一览

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/ping` | 健康检查（免登录） |
| POST | `/api/auth/login` | 登录，返回 JWT（免登录） |
| GET | `/api/auth/me` | 当前登录人 |
| POST | `/api/expense-forms` | 创建报销单（主表 + 明细，事务） |
| GET | `/api/expense-forms` | 分页查询（状态 / 科目 / 申请人 / 关键字 / 日期区间） |
| GET | `/api/expense-forms/{id}` | 详情（含明细） |
| POST | `/api/expense-forms/{id}/submit` | 提交审批 |
| POST | `/api/expense-forms/{id}/approve` | 审批通过 |
| POST | `/api/expense-forms/{id}/reject` | 审批驳回 |
| GET | `/api/expense-forms/{id}/tasks` | 该单的审批节点 |
| GET | `/api/approvals/pending` | 我的待办 |
| GET | `/api/operation-logs/{bizType}/{bizId}` | 操作留痕 |
| GET | `/api/employees`、`/api/employees/{id}` | 员工分页 / 详情 |
| POST | `/api/employees` | 新增员工 |
| GET | `/api/departments` | 部门列表 |

错误码分段：`1xxx` 参数 / `2xxx` 业务 / `401` 未登录 / `5xxx` 系统。

## 项目结构

```
src/main/java/com/maning/expensecontrol/
├── annotation/     @OpLog
├── aspect/         操作留痕切面
├── common/         Result / PageResult / UserContext
├── config/         MyBatis-Plus、WebMvc
├── controller/     Controller 层
├── dto/            入参 DTO 与出参 VO
├── entity/         10 张表的实体
├── exception/      ErrorCode / BusinessException / 全局异常处理
├── mapper/         Mapper 接口（含手写 SQL 与 XML）
├── security/       JwtUtil / PasswordHasher / AuthInterceptor
└── service/        接口 + impl
```

## 已验证的关键场景

| 场景 | 结果 |
|---|---|
| 明细插入失败事务回滚 | 主表与已插入明细零残留（A/B 对照实测） |
| 多表分页 | 列表固定 2 条 SQL，详情固定 5 条 SQL，与明细条数无关 |
| 单号并发生成 | 原方案 20 并发 3 成功；改造后 20 并发 20 成功 |
| 并发审批同一节点 | 5 并发：1 成功 + 4 条「状态已变更」，APPROVE 流水仅 1 条 |
| 无上级提交 | 直接拒绝，不自动通过 |
| 留痕与业务一致性 | 失败场景不落日志；成功链路与审批流水逐条对应 |

## 作者

morning-zy
