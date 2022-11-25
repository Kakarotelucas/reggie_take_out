package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Category;

/**
 * @Date: 2022/11/14 14:29
 * @Auther: cold
 * @Description:
 */
public interface CategoryService extends IService<Category> {
    //定义自己的方法
    public void remove(Long id);
}
