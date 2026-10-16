package com.maning.expensecontrol.exception;

import com.maning.expensecontrol.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 业务异常：Service 主动抛出，属于"预期内的错误"，只记 warn 不打堆栈
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    // @Valid 校验失败：把每个字段的提示拼起来，前端能直接展示
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return Result.fail(ErrorCode.PARAM_INVALID, message);
    }

    // 路径/查询参数类型不对，例如 /api/employees/abc 传了非数字 id
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String message = "参数类型不正确: " + e.getName();
        log.warn("{}", message);
        return Result.fail(ErrorCode.PARAM_INVALID, message);
    }

    // 兜底：任何没被上面接住的异常。必须打完整堆栈，否则线上无从排查 */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleUnknown(Exception e) {
        log.error("未捕获异常", e);
        return Result.fail(ErrorCode.INTERNAL_ERROR);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKey(DuplicateKeyException e) {
        log.warn("唯一索引冲突", e);
        return Result.fail(ErrorCode.EMP_NO_DUPLICATE);
    }


}
