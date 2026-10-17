# 企业费用报销与内控审批系统（expense-control）

## 项目简介
基于 Spring Boot 3 + MyBatis-Plus 的企业费用报销与内控审批系统，
面向"审计信息化"场景，强调审批链留痕与预算控制。

## 技术栈
- Spring Boot 3.5.16 / JDK 21
- MyBatis-Plus 3.5.17 / MySQL 8.0
- Lombok / Jakarta Validation

## 已实现功能
- [x] 统一响应体 `Result<T>` + 分页封装 `PageResult<T>`
- [x] 全局异常处理 + 分段错误码体系（1xxx 参数 / 2xxx 业务 / 5xxx 系统）
- [x] 部门列表查询 / 员工详情 / 员工分页（支持按部门过滤）
- [x] 新增员工（DTO 校验 + 部门存在性 + 工号唯一）

## 数据库设计
8 张表，见 `docs/sql/schema.sql`。设计要点：
- 基础数据用 `status` 停用，业务单据用 `deleted` 逻辑删除，审计记录不设删除字段
- `budget` 表双字段设计：`occupied_amount`（审批中占用）+ `used_amount`（已支付）

## 本地启动
1. 执行 `docs/sql/schema.sql` 与 `data.sql` 建库建表
2. 复制 `application-dev.yml.example` 为 `application-dev.yml`，填入数据库凭据
3. 启动 `ExpenseControlApplication`，访问 `http://localhost:8081/api/ping`

## 接口测试
见 `src/test/java/com/maning/expensecontrol/test/apitest.http`（IDEA HTTP Client）
## 作者

morning-zy
