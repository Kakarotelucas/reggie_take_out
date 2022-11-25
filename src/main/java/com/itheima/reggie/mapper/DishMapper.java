package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Date: 2022/11/15 11:43
 * @Auther: cold
 * @Description:
 */
//@mapper的作用是可以给mapper接口自动生成一个实现类，让spring对mapper接口的bean进行管理，并且可以省略去写复杂的xml文件
@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
