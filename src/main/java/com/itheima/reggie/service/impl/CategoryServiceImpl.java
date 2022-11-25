package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Date: 2022/11/14 14:30
 * @Auther: cold
 * @Description:
 */
@Service //由Spring来管理
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;
    /**
     * 自定义的根据分类id删除分类，删除之前需要判断分类是否关联相应菜品和套餐
     * @param id
     */
    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();

        //添加查询条件，根据菜品Id进行查询
        dishLambdaQueryWrapper.eq(Dish::getId,id);
        int count1 = dishService.count(dishLambdaQueryWrapper);

        //查询当前分类是否关联了菜品，如果已关联则抛出一个业务异常
        if (count1 > 0){

            //已经关联菜品，需要抛出一个业务异常
            throw new CustomException("当前分类关联了菜品，不能删除");
        }

        //查询当前分类是否关联了套餐，如果已关联则抛出一个业务异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();

        //添加查询条件，根据套餐Id进行查询
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);
        if (count2 > 0){

            //已经关联套餐，抛出一个业务异常
            throw new CustomException("当前分类关联了套餐，不能删除");
        }
        //正常删除分类。
        // super是继承的ServiceImpl中的方法.因为接口的remove重写了父类（IService）方法，重写方法内在想调用原来的removeById，只能通过super调用
        super.removeById(id);
    }
}
