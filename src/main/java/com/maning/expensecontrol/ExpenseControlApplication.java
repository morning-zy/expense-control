package com.maning.expensecontrol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 企业费用报销与内控审批系统 —— 启动类。
 *
 * @SpringBootApplication 是三个注解的组合：
 *   @SpringBootConfiguration  —— 声明这是配置类
 *   @EnableAutoConfiguration  —— 按 classpath 上的依赖自动配置（内嵌 Tomcat 就是这么来的）
 *   @ComponentScan            —— 扫描本包及子包下的 @Component / @Service / @Controller
 */
@SpringBootApplication
public class ExpenseControlApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpenseControlApplication.class, args);
    }
}
