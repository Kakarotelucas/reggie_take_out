package com.itheima.reggie.common;

/**
 * @Date: 2022/11/15 12:35
 * @Auther: cold
 * @Description: 自定义业务异常类，用于处理CategoryServiceImpl中的异常
 */
public class CustomException extends RuntimeException{
    public CustomException(String message){
        super(message);
    }
}
