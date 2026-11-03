package com.maning.expensecontrol.aspect;

import com.maning.expensecontrol.annotation.OpLog;
import com.maning.expensecontrol.common.UserContext;
import com.maning.expensecontrol.entity.OperationLog;
import com.maning.expensecontrol.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 操作留痕切面：凡是标了 {@link OpLog} 的方法，成功执行后自动落一条 operation_log。
 * <p>
 * 三个设计要点：
 * 1. 用 @Around 而不是 @AfterReturning —— 只有成功才记，抛异常时不写（事务也会回滚，
 *    写了反而会出现「日志说有、数据库没有」的假记录）。
 * 2. 日志写入失败绝不影响主流程：try-catch 包住，只打 warn。
 * 3. 业务主键支持三种形态：Long 参数、方法返回值、以及「入库后才有 id」的创建场景。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;

    @Around("@annotation(opLog)")
    public Object around(ProceedingJoinPoint pjp, OpLog opLog) throws Throwable {
        Object result = pjp.proceed();      // 先让业务跑，抛异常就直接冒泡，不写日志

        try {
            Long bizId = resolveBizId(pjp, opLog, result);
            Long operatorId = UserContext.getUserId();   // 定时任务等无登录态场景允许为空

            OperationLog entity = new OperationLog();
            entity.setBizType(opLog.bizType());
            entity.setBizId(bizId == null ? 0L : bizId);
            entity.setOperatorId(operatorId == null ? 0L : operatorId);
            entity.setAction(opLog.action());
            entity.setNewValue(buildSummary((MethodSignature) pjp.getSignature(), pjp.getArgs()));
            entity.setIp(currentIp());
            operationLogMapper.insert(entity);
        } catch (Exception e) {
            // 留痕失败不能把业务带崩
            log.warn("写操作留痕失败：bizType={}, action={}, err={}",
                    opLog.bizType(), opLog.action(), e.getMessage());
        }
        return result;
    }

    /**
     * 业务主键来源，按优先级：
     * 1. 第 bizIdArgIndex 个入参本身就是 Long / Number —— 适用于 approve(reject) 这类只传 id 的方法
     * 2. 方法返回值是 Number —— 适用于 create 这类「插入后才拿到 id」的场景
     * 3. 都没有就记 0（至少留下「谁在什么时间做了什么」）
     */
    private Long resolveBizId(ProceedingJoinPoint pjp, OpLog opLog, Object result) {
        int idx = opLog.bizIdArgIndex();
        Object[] args = pjp.getArgs();
        if (idx >= 0 && idx < args.length) {
            Object arg = args[idx];
            if (arg instanceof Number n) {
                return n.longValue();
            }
        }
        if (result instanceof Number n) {
            return n.longValue();
        }
        return null;
    }

    /** 入参摘要，截断到 500 字，避免把整个大对象刷进日志表 */
    private String buildSummary(MethodSignature signature, Object[] args) {
        String method = signature.getName();
        if (args == null || args.length == 0) {
            return method;
        }
        StringBuilder sb = new StringBuilder(method).append(": ");
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            sb.append(stripQuotes(pack(arg))).append("; ");
        }
        String summary = sb.toString();
        return summary.length() > 500 ? summary.substring(0, 500) : summary;
    }

    /**
     * 把入参压成可读文本。
     * Lombok 自动生成的 toString() 格式是「类名(字段=值, 字段=值)」，
     * 默认全限定类名又长又没用，这里把包名前缀去掉再取括号里的内容。
     */
    private String pack(Object arg) {
        if (arg instanceof CharSequence || arg instanceof Number || arg instanceof Boolean) {
            return String.valueOf(arg);
        }
        String text = String.valueOf(arg);
        int left = text.indexOf('(');
        int right = text.lastIndexOf(')');
        if (left > 0 && right > left) {
            return text.substring(left + 1, right);
        }
        return text;
    }

    private String stripQuotes(String text) {
        return text.replace("\\\"", "\"").replace("\"", "'");
    }

    private String currentIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return null;
            }
            HttpServletRequest request = attrs.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();   // 经过 nginx 时取第一个真实客户端 ip
            }
            return request.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }
}
