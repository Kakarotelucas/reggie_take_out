package com.itheima.reggie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @Date: 2022/11/3 12:17
 * @Auther: cold
 * @Description: ReggieApplication启动类
 */
//自定义log日志
@Slf4j

//扫描filter中的@WebFilter注解
@ServletComponentScan

//定义为SpringBoot启动类
@SpringBootApplication

//多张表操作，开启事务支持
@EnableTransactionManagement

public class ReggieApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReggieApplication.class);
        log.info("项目启动成功");
    }
}
