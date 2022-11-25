package com.itheima.reggie.dto;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.entity.Setmeal;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
