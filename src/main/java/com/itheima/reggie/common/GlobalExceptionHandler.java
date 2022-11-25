package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * @Date: 2022/11/7 12:30
 * @Auther: cold
 * @Description: 全局异常捕获,全局异常拦截底层是代理，通过拦截Controller进行相关处理。
 */
//指定要拦截的Controller(EmployeeController中就加了@RestController)
@ControllerAdvice(annotations = {RestController.class, Controller.class})

//@ResponseBody这个注解表示你的返回值将存在responsebody中返回到前端，也就是将return返回值作为请求返回值，return的数据不会解析成返回跳转路径，将java对象转为json格式的数据，前端接收后会显示将数据到页面，如果不加的话 返回值将会作为url的一部分，页面会跳转到这个url，也就是跳转到你返回的这个路径
@ResponseBody
@Slf4j //日志
public class GlobalExceptionHandler {

    /**
     * 避免新增员工时出现用户名不唯一等异常。
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException. class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){

        log.error(ex.getMessage());


        if(ex.getMessage().contains("Duplicate entry")){
            //通过空格分隔异常信息：“zhangsan”位于数组索引2
            String[] split = ex.getMessage().split(" ");
            String msg = split[2] + "已存在";
            return R.error(msg);
        }
        return R.error("未知错误");
    }


    /**
     * 自定义CustomException异常处理方法
     * @return
     */
    @ExceptionHandler(CustomException. class)
    public R<String> exceptionHandler(CustomException ex){

        log.error(ex.getMessage());

        return R.error(ex.getMessage());
    }
}
