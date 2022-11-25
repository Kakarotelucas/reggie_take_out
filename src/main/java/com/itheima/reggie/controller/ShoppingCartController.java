package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Date: 2022/11/23 21:35
 * @Auther: cold
 * @Description: 购物车
 */
@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     *
     * @param shoppingCart
     * @return http://localhost:8080/shoppingCart/add
     * 传递载荷：{
     * "amount": 55,
     * "setmealId": "1594618768242245633",
     * "name": "商务套餐A",
     * "image": "5e1eb1d8-47f9-4680-83b5-561cd5c22c96.jpg"
     * }
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info("购物车数据:{}", shoppingCart);

        //1、设置用户id，指定当前是哪个用户的购物车数据（不同用户登陆购物车当然不一样）
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        Long dishId = shoppingCart.getDishId();

        //根据用户id查询菜品id联合查询才能确定是套餐还是菜品
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);

        if (dishId != null) {
            //添加到购物车的是菜品
            queryWrapper.eq(ShoppingCart::getDishId, dishId);

        } else {
            //添加到购物车的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        //2、查询当前菜品或者套餐是否在购物车中，用来确定用户是否同一菜品添加两次购物车
        //SQL:select * from shopping_cart where user_id = ? and dish_id/setmeal_id = ?
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);

        if (cartServiceOne != null) {
            //3、如果已经存在，就在原来数量基础上加一
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number + 1);
            shoppingCartService.updateById(cartServiceOne);
        } else {
            //4、如果不存在，则添加到购物车，数量默认就是一
            shoppingCart.setNumber(1);

            //设置入库时间，后续可以进行排序
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne = shoppingCart;
        }

        //返回购物车对象
        return R.success(cartServiceOne);
    }

    /**
     * 查看购物车
     *
     * @return http://localhost:8080/shoppingCart/list
     * 预览
     * {
     * "code": 1,
     * "msg": null,
     * "data": [
     * {
     * "id": "1595414654056734722",
     * "name": "商务套餐A",
     * "userId": "1595333680434249730",
     * "dishId": null,
     * "setmealId": "1594618768242245633",
     * "dishFlavor": null,
     * "number": 2,
     * "amount": 55,
     * "image": "5e1eb1d8-47f9-4680-83b5-561cd5c22c96.jpg",
     * "createTime": "2022-11-23 21:51:10"
     * }
     * ],
     * "map": {}
     * }
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        log.info("查看购物车");

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper();

        //根据userid查询
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

        //根据创建时间升序排列（最后加入的菜品最先展示）
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 清空购物车
     * http://localhost:8080/shoppingCart/clean
     *
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean() {
        //要发的SQL：delete from shopping_cart where user_id = ?

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

        shoppingCartService.remove(queryWrapper);

        return R.success("清空购物车成功");
    }

    /**
     * 补充：购物车中菜品数量-1
     * @param shoppingCart
     * @return http://localhost:8080/shoppingCart/sub
     * 传过来的荷载
     * {
     *   "dishId": "1397854865672679425",
     *   "setmealId": null
     * }
     */

    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {

        //1、设置用户id，指定当前是哪个用户的购物车数据（不同用户登陆购物车当然不一样）
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        Long dishId = shoppingCart.getDishId();

        //根据用户id查询菜品id联合查询才能确定是套餐还是菜品
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);

        if (dishId != null) {
            //添加到购物车的是菜品
            queryWrapper.eq(ShoppingCart::getDishId, dishId);

        } else {
            //添加到购物车的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        //2、查询当前菜品或者套餐是否在购物车中，用来确定用户是否同一菜品添加两次购物车
        //SQL:select * from shopping_cart where user_id = ? and dish_id/setmeal_id = ?

        //得到一个购物车对象
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);

        //得到购物车的菜品数量
        Integer dishNumber = cartServiceOne.getNumber();

        if (dishNumber == 1) {
            //3、只剩下一个菜品，删除购物车
            shoppingCartService.remove(queryWrapper);
        } else {
            //不止一个菜品，菜品数量减1
            cartServiceOne.setNumber(dishNumber - 1);
            shoppingCartService.updateById(cartServiceOne);
        }
        //返回购物车对象
        return R.success(cartServiceOne);
    }
}


