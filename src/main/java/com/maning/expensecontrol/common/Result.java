package com.maning.expensecontrol.common;

import com.maning.expensecontrol.exception.ErrorCode;
import lombok.Getter;

@Getter
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    //三个静态工厂方法
    public static <T> Result<T> ok(T data){
        return new Result<>(0,"success",data);
    }
    public static <T> Result<T> ok(){
        return new Result<>(0,"success",null);
    }
    public static <T> Result<T> fail(String message){
        return new Result<>(1,message,null);
    }

    public static <T> Result<T> fail(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> Result<T> fail(ErrorCode errorCode, String message) {
        return new Result<>(errorCode.getCode(), message, null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }
}
