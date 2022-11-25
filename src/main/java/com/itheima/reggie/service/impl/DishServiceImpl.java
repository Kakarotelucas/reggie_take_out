package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.service.DishFlavorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Date: 2022/11/15 11:45
 * @Auther: cold
 * @Description:
 */
@Slf4j
@Service //由Spring来管理
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;
    /**
     * 新增菜品，同时保存对应的口味数据
     * @param dishDto
     */

    //多张表操作，开启事务.注解在Service接口用更好
    @Transactional
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);

        //获得菜品id
        Long dishId = dishDto.getId();

        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();

        //处理集合
        //遍历每个DishFlavor并为其id赋值
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
            //用stream返回list
        }).collect(Collectors.toList());

        //保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);
    }

    /**
     *根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {

        //查询菜品基本信息，从dish表查询
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();

        //为dishDto拷贝除了口味外的常规属性
        BeanUtils.copyProperties(dish, dishDto);

        //查询当前菜品对应的口味信息，从dish_flavor表查询
        //构造条件构造器
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();

        //添加条件
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());

        //查询
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);

        //单独为dishDto附上flavors数据
        dishDto.setFlavors(flavors);
        return dishDto;
    }

    /**
     *更新菜品信息，同时更新对应的口味信息
     * @param dishDto
     */
    @Override
    @Transactional //事务注解，保证事务的一致性
    public void updateWithFlavor(DishDto dishDto) {
        //更新菜品表基本信息,dishDto是dish的子类
        this.updateById(dishDto);

        //先清理当前菜品对应的口味数据——dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper();

        //条件
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        //再对提交过来的口味数据进行添加——dish——flavor的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();

        //处理集合
        //原本菜品id没有值，需要遍历每个DishFlavor并为其id赋值
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
            //用stream返回list
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }
}
