package com.itheima.reggie.dto;


import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据传输对象，单独Dish类数据不够封装，用于封装接收页面提交的数据
 */
@Data
public class DishDto extends Dish {

    //菜品所对应的口味数据
    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
