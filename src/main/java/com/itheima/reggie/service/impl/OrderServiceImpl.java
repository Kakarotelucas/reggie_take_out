package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrderMapper;
import com.itheima.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired //用于查询购物车数据
    private ShoppingCartService shoppingCartService;

    @Autowired //用于查询用户数据
    private UserService userService;

    @Autowired //用于查询用户地址
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     * @param orders
     */
    @Transactional
    public void submit(Orders orders) {
        //1、首先得知道用户是谁：获得当前用户id
        Long userId = BaseContext.getCurrentId();

        //2、需要知道买的是哪些商品：从购物车中获取。查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();

        /*根据用户id来查商品*/
        wrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(wrapper);

        //购物车不为空再进行下单
        if(shoppingCarts == null || shoppingCarts.size() == 0){
            throw new CustomException("购物车为空，不能下单");
        }

        //查询用户数据，因为需要使用到地址、电话等信息
        User user = userService.getById(userId);

        //查询地址数据
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);

        //地址不为空再进行下单
        if(addressBook == null){
            throw new CustomException("用户地址信息有误，不能下单");
        }

        //2.1保存之前需要先给orders对象设置空缺的值
        long orderId = IdWorker.getId();//订单号

        //计算购物车总金额，这里使用原子类AtomicInteger就是可以保证在多线程情况下计算不会出错，例如int类型在高并发多线程情况下可能导致运算出错
        AtomicInteger amount = new AtomicInteger(0); //AtomicInteger( 0 )表示：无参的构造方法默认值为0

        //订单明细数据
        List<OrderDetail> orderDetails =

            //将整个购物车数据遍历
            shoppingCarts.stream().map((item) -> {

            //新建一个订单明细对象
            OrderDetail orderDetail = new OrderDetail();

            //设置具体参数
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());//单份菜品金额

                //进行累加，相当于 +=  把每次遍历出来的值进行累加

            amount.addAndGet(item.getAmount(). //每次遍历的单份金额
                    //单份金额乘以份数(new BigDecimal(item.getNumber()))  ——在定义字段要求精度比较高的时候，一般会使用BigDecimal类型
                    multiply(new BigDecimal(item.getNumber())).intValue()); //转成intValue用于输出int数据
            return orderDetail;
        }).collect(Collectors.toList());

        //2.2补充的空缺数据
        orders.setId(orderId); //订单号
        orders.setOrderTime(LocalDateTime.now()); //设置的下单时间
        orders.setCheckoutTime(LocalDateTime.now()); // 设置的支付时间
        orders.setStatus(2); //配送状态：待派送
        orders.setAmount(new BigDecimal(amount.get()));//订单总金额
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee()); //收货人
        orders.setPhone(addressBook.getPhone()); //收货人电话

        //从地址簿里将省市区拼接起来
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        //3、向订单表插入数据，一个订单对应一条数据，保存即可
        this.save(orders);

        //4、向订单明细表插入数据，多个菜品或者饮品可能有多条数据
        orderDetailService.saveBatch(orderDetails);

        //5、下单完成后需要清空购物车数据
        shoppingCartService.remove(wrapper);
    }
}