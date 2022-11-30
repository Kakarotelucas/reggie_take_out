package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Date: 2022/11/17 17:19
 * @Auther: cold
 * @Description: 菜品管理
 */
@Slf4j
//@RestController的作用等同于@Controller + @ResponseBody
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){

        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        //清理所有类别菜品
        /*Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);*/

        //清理某个类别下的菜品
        String key = "dish_" + dishDto.getCategoryId() + "_1"; //因为前端提交过来的都是状态为1
        redisTemplate.delete(key);

        return R.success("新增菜品成功");
    }


    /**
     *菜品分类查询，不仅要展示菜品基本信息，还要展示菜品分类所对应的名称
     * @param page
     * @param pageSize
     * @param name 搜索框输入的值
     * 请求参数：http://localhost:8080/dish/page?page=1&pageSize=10&name=123
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {

        //构造分页构造器对象
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        //单独Dish参数展示不全，需要借助数据传输对象进行展示,再进行对象拷贝
        Page<DishDto> dishDtoPage = new Page<>();

        //构造条件过滤器：动态封装查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        //添加过滤条件：根据name进行查询
        queryWrapper.like(name != null, Dish::getName, name);

        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo, queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(
                //拷贝的源
                pageInfo,
                //拷贝到哪
                dishDtoPage,
                //忽略的属性（Page对象中的records——列表展示）
                "records"
        );

        List<Dish> records = pageInfo.getRecords();

        //把records利用stream流进行处理
        //item指遍历出来的Dish
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            //避免给dishDto对象赋值时，除CategoryName以外的值为null，所以需要进行拷贝
            BeanUtils.copyProperties(item, dishDto);//(源对象，拷贝到哪)

            //分类id
            Long categoryId = item.getCategoryId();

            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            //避免出现没有分类的情况
            if (category != null){
                //得到分类名称
                String categoryName = category.getName();

                //给dishDto对象赋值
                dishDto.setCategoryName(categoryName);
            }

            return dishDto;
        })//收集处理好的dishDto转成List集合
        .collect(Collectors.toList());

        //将处理好的list赋值给dishDtoPage对象
        dishDtoPage.setRecords(list);

        //return R.success(pageInfo); //因为Dish中不含有菜品分类字段，造成数据缺失，菜品分类为空，其他可以正常展示,要借助DishDto中的categoryName进行展示

        return R.success(dishDtoPage);
    }

    /**
     * 菜品管理中的根据id查询菜品信息和对应的口味信息
     * @param id
     * @return 请求为：http://localhost:8080/dish/1413384757047271425
     */
    @GetMapping("/{id}")  //要获得id需要用到 @PathVariable
    //这里不用R<DishDto>不用Dish是因为Dish里面没有口味
    public R<DishDto> get(@PathVariable Long id){

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }


    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){

        log.info(dishDto.toString());

        dishService.updateWithFlavor(dishDto);

        //清理所有类别菜品
        /*Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);*/

        //为了保证数据库中的数据和缓存中的数据一致：清理某个类别下的菜品
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return R.success("修改菜品成功");
    }

    /**
     *新增套餐时需要选定一些菜品，根据条件查询对应的菜品数据（套餐）
     * @param dish
     * @return 请求地址：http://localhost:8080/dish/list?categoryId=1397849739276890114
     */
    /*@GetMapping("/list") //可以用Long categoryId,但是用Dish dish通用性更强
    public R<List> list(Dish dish){

        //构造查询条件对象
        LambdaQueryWrapper<Dish> queryWrapper =new LambdaQueryWrapper<>();

        //构造查询条件
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());

        //只查询起售状态(为1)的菜品
        queryWrapper.eq(Dish::getStatus, 1);

        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        return R.success(list);
    }*/


    //新增套餐时需要选定一些菜品，根据条件查询对应的菜品数据（套餐）
    //请求地址：http://localhost:8080/dish/list?categoryId=1397849739276890114
    //优化：同时移动端菜品点击加号需要显示口味信息，而不只是菜品基本信息
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){

        //动态构造一个key
        List<DishDto> dishDtoList = null;
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();//dish_1397849739276890114_1

        //优化：1.1先从Redis获取缓存数据（按照湘菜川菜每个类别id和状态即上面的key进行分类获取）
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        //1.2如果存在，直接返回，无需查询数据库
        if (dishDtoList != null){
            return R.success(dishDtoList);
        }


        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null ,Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus,1);

        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            //获得当前菜品的id
            Long dishId = item.getId();

            //拿着获取到的菜品id来查询菜品口味
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);

            //SQL:select * from dish_flavor where dish_id = ?查出来一个口味集合
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);

            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());

        //1.3如果不存在，需要查询数据库，将查询到的菜品缓存到Redis
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }
}
