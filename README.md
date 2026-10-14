# 企业费用报销与内部审批系统



## 背景

当下中小企业报销依赖线下单据，审批链路不透明，预算超支无法事前拦截，出了问题查不到是谁改的。

## 技术栈

| 组件          | 版本     |
|-------------|--------|
| JDK         | 21     |
| Spring Boot | 3.5.16 |
| Maven       | 3.9.16 |
| MySQL       | 8.0.46 |

## 本地启动

前置条件：JDK 21 / Maven 3.9+ / MySQL 8

**1. 克隆或打开工程**

**2. 启动应用**
```bash
mvn spring-boot:run
```
**3. 验证**

浏览器访问 `http://localhost:8081/api/ping`，应返回：

```json
{"code":0,"message":"success","data":{"projectName":"expense-control","currentTime":"2026-10-14T22:17:57.9501718","author":"morning-zy"}}
```

## 目录结构

    src/main/java/com/maning/expensecontrol/
      ├── controller/   接收请求
      ├── common/       通用组件（统一响应体等）
      └── ...

## 作者

morning-zy
