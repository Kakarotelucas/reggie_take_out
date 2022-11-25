package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import com.itheima.reggie.mapper.EmployeeMapper;
import org.springframework.stereotype.Service;

/**
* @author cold
* @description 针对表【employee(员工信息)】的数据库操作Service实现
* @createDate 2022-11-03 16:13:40
*/
//由Spring来管理
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee>
    implements EmployeeService{

}




