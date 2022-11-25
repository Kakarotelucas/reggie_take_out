package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

/**
* @author cold
* @description 针对表【employee(员工信息)】的数据库操作Mapper
* @createDate 2022-11-03 16:13:40
* @Entity com.reggie.pojo.Employee
*/

//@mapper的作用是可以给mapper接口自动生成一个实现类，让spring对mapper接口的bean进行管理，并且可以省略去写复杂的xml文件
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

}




