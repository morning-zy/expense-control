-- ============================================================
--  企业费用报销与内控审批系统 —— 表结构（DDL）
--  库名     : expense_control
--  字符集   : utf8mb4 / utf8mb4_0900_ai_ci
--  适配版本 : MySQL 8.0.46
--  执行方式 : mysql -uroot -p < docs/sql/schema.sql
--  说明     : 脚本可重复执行（先 DROP 再 CREATE），但会清空数据
-- ============================================================

CREATE DATABASE IF NOT EXISTS `expense_control`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `expense_control`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------
-- 1. sys_department 部门
--    为保持表结构可重复执行，先删后建
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `sys_department`;
CREATE TABLE `sys_department` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT           COMMENT '主键',
  `dept_code`  VARCHAR(32) NOT NULL                          COMMENT '部门编码，业务唯一标识',
  `dept_name`  VARCHAR(64) NOT NULL                          COMMENT '部门名称',
  `parent_id`  BIGINT      NOT NULL DEFAULT 0                COMMENT '上级部门 id，0 表示顶级',
  `manager_id` BIGINT          NULL                          COMMENT '部门负责人（sys_employee.id）',
  `status`     TINYINT     NOT NULL DEFAULT 1                COMMENT '状态：1 启用 / 0 停用',
  `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dept_code` (`dept_code`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '部门';

-- ------------------------------------------------------------
-- 2. sys_employee 员工
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `sys_employee`;
CREATE TABLE `sys_employee` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT           COMMENT '主键',
  `emp_no`     VARCHAR(32) NOT NULL                          COMMENT '工号',
  `name`       VARCHAR(64) NOT NULL                          COMMENT '姓名',
  `dept_id`    BIGINT      NOT NULL                          COMMENT '所属部门 id',
  `position`   VARCHAR(64)     NULL                          COMMENT '职位',
  `job_level`  TINYINT     NOT NULL DEFAULT 1                COMMENT '职级，1 最低；审批规则会用到',
  `manager_id` BIGINT          NULL                          COMMENT '直属主管 id，总经理为空',
  `status`     TINYINT     NOT NULL DEFAULT 1                COMMENT '状态：1 在职 / 0 离职',
  `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_emp_no` (`emp_no`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_manager_id` (`manager_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工';

-- ------------------------------------------------------------
-- 3. expense_category 费用科目
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `expense_category`;
CREATE TABLE `expense_category` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT      COMMENT '主键',
  `category_code`   VARCHAR(32)   NOT NULL                     COMMENT '科目编码',
  `category_name`   VARCHAR(64)   NOT NULL                     COMMENT '科目名称：差旅费 / 办公费 / 业务招待费 / 交通费',
  `require_invoice` TINYINT       NOT NULL DEFAULT 1           COMMENT '是否强制要求发票：1 是 / 0 否',
  `single_limit`    DECIMAL(12,2)     NULL                     COMMENT '单笔限额，NULL 表示不限',
  `status`          TINYINT       NOT NULL DEFAULT 1           COMMENT '状态：1 启用 / 0 停用',
  `created_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_code` (`category_code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '费用科目';

-- ------------------------------------------------------------
-- 4. budget 预算
--    「内控」的核心：占用与使用分两个字段
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `budget`;
CREATE TABLE `budget` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT      COMMENT '主键',
  `dept_id`         BIGINT        NOT NULL                     COMMENT '部门 id',
  `category_id`     BIGINT        NOT NULL                     COMMENT '科目 id',
  `period_type`     TINYINT       NOT NULL                     COMMENT '周期类型：1 年度 / 2 季度 / 3 月度',
  `period`          VARCHAR(16)   NOT NULL                     COMMENT '周期标识：2026 / 2026Q4 / 2026-11',
  `total_amount`    DECIMAL(14,2) NOT NULL DEFAULT 0.00        COMMENT '预算总额',
  `occupied_amount` DECIMAL(14,2) NOT NULL DEFAULT 0.00        COMMENT '已占用：审批中的单据占着，尚未实际支出',
  `used_amount`     DECIMAL(14,2) NOT NULL DEFAULT 0.00        COMMENT '已使用：已审批通过并完成支付的金额',
  `version`         INT           NOT NULL DEFAULT 0           COMMENT '乐观锁版本号',
  `status`          TINYINT       NOT NULL DEFAULT 1           COMMENT '状态：1 生效 / 0 作废',
  `created_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_budget_scope` (`dept_id`, `category_id`, `period_type`, `period`),
  KEY `idx_period` (`period_type`, `period`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '预算';

-- ------------------------------------------------------------
-- 5. expense_form 报销单主表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `expense_form`;
CREATE TABLE `expense_form` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT         COMMENT '主键',
  `form_no`      VARCHAR(32)   NOT NULL                        COMMENT '单号，规则 BX + yyyyMM + 4 位流水，如 BX2026100001',
  `applicant_id` BIGINT        NOT NULL                        COMMENT '申请人（sys_employee.id）',
  `dept_id`      BIGINT        NOT NULL                        COMMENT '申请部门 id（冗余字段，列表查询免 join）',
  `category_id`  BIGINT        NOT NULL                        COMMENT '费用科目 id',
  `total_amount` DECIMAL(14,2) NOT NULL DEFAULT 0.00           COMMENT '报销总额',
  `item_count`   INT           NOT NULL DEFAULT 0              COMMENT '明细条数（冗余字段，列表页展示用）',
  `reason`       VARCHAR(500)      NULL                        COMMENT '报销事由',
  `status`       VARCHAR(16)   NOT NULL DEFAULT 'DRAFT'        COMMENT '状态：DRAFT/SUBMITTED/APPROVING/APPROVED/REJECTED/CANCELLED/PAID',
  `current_node` INT           NOT NULL DEFAULT 0              COMMENT '当前审批节点序号，0 表示未进入审批',
  `total_node`   INT           NOT NULL DEFAULT 0              COMMENT '总审批节点数，提交时按金额规则算出',
  `submit_time`  DATETIME          NULL                        COMMENT '提交时间',
  `finish_time`  DATETIME          NULL                        COMMENT '审批完成（通过/驳回）时间',
  `version`      INT           NOT NULL DEFAULT 0              COMMENT '乐观锁版本号',
  `deleted`      TINYINT       NOT NULL DEFAULT 0              COMMENT '逻辑删除：0 正常 / 1 已删除',
  `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_form_no` (`form_no`),
  KEY `idx_applicant_status` (`applicant_id`, `status`),
  KEY `idx_dept_created` (`dept_id`, `created_at`),
  KEY `idx_status_created` (`status`, `created_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '报销单主表';

-- ------------------------------------------------------------
-- 6. expense_item 报销明细
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `expense_item`;
CREATE TABLE `expense_item` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT         COMMENT '主键',
  `form_id`      BIGINT        NOT NULL                        COMMENT '所属报销单 id',
  `expense_date` DATE          NOT NULL                        COMMENT '费用发生日期',
  `amount`       DECIMAL(12,2) NOT NULL                        COMMENT '金额',
  `description`  VARCHAR(255)      NULL                        COMMENT '费用说明',
  `invoice_no`   VARCHAR(64)       NULL                        COMMENT '发票号码',
  `invoice_code` VARCHAR(32)       NULL                        COMMENT '发票代码',
  `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_form_id` (`form_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '报销明细';

-- ------------------------------------------------------------
-- 7. approval_record 审批记录（审计素材，不设删除字段）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `approval_record`;
CREATE TABLE `approval_record` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT      COMMENT '主键',
  `form_id`          BIGINT       NOT NULL                     COMMENT '报销单 id',
  `node_seq`         INT          NOT NULL                     COMMENT '节点序号 1/2/3',
  `node_name`        VARCHAR(64)  NOT NULL                     COMMENT '节点名称：主管审批 / 部门审批 / 财务审批',
  `approver_id`      BIGINT       NOT NULL                     COMMENT '审批人 id',
  `action`           VARCHAR(16)  NOT NULL                     COMMENT '动作：APPROVE / REJECT',
  `approval_comment` VARCHAR(500)     NULL                     COMMENT '审批意见',
  `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
  PRIMARY KEY (`id`),
  KEY `idx_form_id` (`form_id`),
  KEY `idx_approver_action` (`approver_id`, `action`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批记录';

-- ------------------------------------------------------------
-- 8. operation_log 操作留痕（字段级变更，审计轨迹）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT           COMMENT '主键',
  `biz_type`    VARCHAR(32)  NOT NULL                          COMMENT '业务类型：EXPENSE_FORM / BUDGET',
  `biz_id`      BIGINT       NOT NULL                          COMMENT '业务主键',
  `operator_id` BIGINT       NOT NULL                          COMMENT '操作人 id',
  `action`      VARCHAR(32)  NOT NULL                          COMMENT '动作：CREATE / UPDATE / SUBMIT / APPROVE / REJECT / CANCEL',
  `field_name`  VARCHAR(64)      NULL                          COMMENT '变更的字段名，非字段级操作为空',
  `old_value`   VARCHAR(500)     NULL                          COMMENT '旧值',
  `new_value`   VARCHAR(500)     NULL                          COMMENT '新值',
  `ip`          VARCHAR(64)      NULL                          COMMENT '操作来源 IP',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_biz` (`biz_type`, `biz_id`),
  KEY `idx_operator_created` (`operator_id`, `created_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '操作留痕';

SET FOREIGN_KEY_CHECKS = 1;
