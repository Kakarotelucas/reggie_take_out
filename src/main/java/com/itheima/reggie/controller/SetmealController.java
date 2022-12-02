package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Date: 2022/11/20 05:54
 * @Auther: cold
 * @Description: 套餐管理
 */
//@RestController的作用等同于@Controller + @ResponseBody
@RestController
//通过 RequestMapping 注解来处理这些映射请求，也就是通过它来指定控制器可以处理哪些URL请求
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {


    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;


    /**
     *新增套餐
     * @param setmealDto
     * @return 请求：是一大串各个菜品的合集
     */
    @PostMapping//@RequestBody主要用来接收前端传递给后端的json字符串中的数据的(请求体中的数据的)；而最常用的使用请求体传参的无疑是POST请求了，所以使用@RequestBody接收数据时，一般都用POST方式进行提交

    //CacheEvict：清理指定缓存
    //value：缓存的名称，每个缓存名称下面可以有多个key
    @CacheEvict(value = "setmealCache", allEntries = true) //allEntries = true表示删除setmealCache下的所有数据
    public R<String> save(@RequestBody SetmealDto setmealDto){

        log.info("套餐信息：{}", setmealDto);

        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }


    /**
     * 套餐分页查询,页面中的套餐名称需要用到数据传输对象SetmealDto
     * @param page
     * @param pageSize
     * @param name
     * @return 请求地址：http://localhost:8080/setmeal/page?page=1&pageSize=10&name=123
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){

        //构造分页构造器
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);

        Page<SetmealDto> dtoPage = new Page<>();

        //
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();

        //添加条件构造器,根据name进行模糊查询
        queryWrapper.like(name != null, Setmeal::getName, name);

        //添加排序条件,根据更新时间降序排序
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        //进行分页查询
        setmealService.page(pageInfo, queryWrapper);

        //对象拷贝,忽略的属性（Page对象中的records——分页列表展示的记录，因为两个分页构造器泛型不一样）
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");

        //自己设置records
        List<Setmeal> records = pageInfo.getRecords();

        //将records经过处理后转成泛型为SetmealDto的集合
        List<SetmealDto> list =

            records.stream().map((item) -> {

            SetmealDto setmealDto = new SetmealDto();
            //为缺少的数据赋值
            BeanUtils.copyProperties(item, setmealDto);

            //获取分类id
            Long categoryId = item.getCategoryId();

            //根据分类id查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null){
                //分类名称
                String categoryName = category.getName();
                //为setmealDto设置分类名称
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;

        }).collect(Collectors.toList());

        //为dtoPage设置处理过后的records
        dtoPage.setRecords(list);
        return R.success(dtoPage);

    }

    /**
     * 删除套餐,对应的关联关系数据也需要删除
     * @param ids
     * @return 请求网址：http://localhost:8080/setmeal?ids=1415580119015145474,1594248889081393154
     */
    @DeleteMapping

    //CacheEvict：清理指定缓存
    //value：缓存的名称，每个缓存名称下面可以有多个key
    @CacheEvict(value = "setmealCache", allEntries = true) //allEntries = true表示删除setmealCache下的所有数据
    //@RequestParam用于将指定参数赋值给方法中的形参。意思就是标注浏览器地址栏参数名称
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);

        setmealService.removeWithDish(ids);

        return R.success("套餐数据删除成功");
    }

    /**
     * 移动端点击套餐显示套餐功能
     * @param setmeal
     * @return http://localhost:8080/setmeal/list?categoryId=1413342269393674242&status=1,不是JSON数据，不用加RequestBody
     */
    @GetMapping("/list")

    //Cacheable：在方法执行前spring先查看缓存中是否有数据，如果有数据，则直接返回缓存数据；若没有数据，调用方法并将方法返回值放到缓存中
    //value：缓存的名称，每个缓存名称下面可以有多个key
    //key：缓存的key
    @Cacheable(value = "setmealCache", key = "#setmeal.categoryId + '_' + #setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        log.info("setmeal:{}", setmeal);
        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(setmeal.getName()), Setmeal::getName, setmeal.getName());
        queryWrapper.eq(null != setmeal.getCategoryId(), Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(null != setmeal.getStatus(), Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        return R.success(setmealService.list(queryWrapper));
    }
}
