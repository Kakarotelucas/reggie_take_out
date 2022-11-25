package com.itheima.reggie.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Date: 2022/11/3 15:35
 * @Auther: cold
 * @Description: Employee实体类
 */
@Data

//类通过实现 java.io.Serializable 接口以启用其序列化功能
public class Employee implements Serializable {

    //private static final long serialVersionUID=1L意思是定义程序序列化ID。序列化ID等同于身份验证，主要用于程序的版本控制，维护不同版本的兼容性以及避免在程序版本升级时程序报告的错误
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String username;

    private String password;

    private String phone;

    private String sex;

    private String idNumber; //身份证号码

    private Integer status;

    //使用@TableField注解标记与公共字段填充处理类配合就可以填充公共字段
    @TableField(fill = FieldFill.INSERT) //插入时填充字段
    private LocalDateTime createTime; //JDK1.8后的新时间类。java.time包中提供了四个常用的时间类，分别是Instant、ocalDate、LocalTime、LocalDateTime

    @TableField(fill = FieldFill.INSERT_UPDATE) //插入和更新时填充字段
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT) //插入时填充字段
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE) //插入和更新时填充字段
    private Long updateUser;

}
