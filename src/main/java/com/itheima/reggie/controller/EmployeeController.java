package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.service.EmployeeService;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @Date: 2022/11/3 16:34
 * @Auther: cold
 * @Description:
 */
//输出日志
@Slf4j

//@RestController的作用等同于@Controller + @ResponseBody
@RestController

//通过 RequestMapping 注解来处理这些映射请求，也就是通过它来指定控制器可以处理哪些URL请求
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired//@Autowired:给引用类型按类型注入,也可以用@Resource
    private EmployeeService employeeService;

    /**
     * 员工登陆
     * @param request
     * @param employee
     * @return
     */
    //@PostMapping注解用于接收和处理Post方式的请求，因为前端发送的请求为post
    @PostMapping("/login")
    //返回值为R，泛型是Employee
    // @RequestBody将传来的JSON数据（username，password）放入到响应体中。
    //HttpServletRequest request，用于登陆后将employee对象的id存到session表示登陆成功，可以get session用于获取登陆用户
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){

        //1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();

        //调用工具类完成 MD5加密
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2、根据页面提交的用户名username查询数据库 LambdaQueryWrapper与QueryWrapper查询类似，不过使用的是Lambda语法
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();

        //根据用户名和传来的用户名进行等值查询
        queryWrapper.eq(Employee::getUsername, employee.getUsername());

        //调用employeeService的getOne方法得到一个employee对象，因为用户名加了unique索引，是唯一的
        Employee emp = employeeService.getOne(queryWrapper);

        //3、如果没有查询到则返回登录失败结果
        if (emp == null){
            return R.error("登陆失败");
        }

        //4、密码比对，如果不一致则返回登录失败结果(数据库里查的密码跟页面提交MD5处理完后的密码比对)
        if (!emp.getPassword().equals(password)){
            return R.error("登陆失败");
        }

        //5、密码正确，进一步查看员工状态，如果为已禁用状态，则返回员工已禁用结果(0为禁用，1为可用)
        if (emp.getStatus() == 0){
            return R.error("账号已禁用");
        }

        //6、账号没有禁用，登录成功，将员工id存入Session并返回登录成功结果
        //request.getSession().setAttribute(“绑定名”,绑定值);这段代码的意思就是：获取session对象,然后把要绑定对象/值 帮定到session对象上,
        // 用户的一次会话共享一个session对象用户。登陆成功后服务器将用户的信息绑定到session对象上，之后你的每一个请求服务器都要去查看这个绑定的session
        // 是否存在，如果存在证明是登陆成功可以访问，如果不存在证明是没有登录的用户，返回登录页面
        request.getSession().setAttribute("employee", emp.getId());

        return R.success(emp);
    }


    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){

        //清除Session中保存的当前登录的员工信息
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");

    }

    /**
     * 新增员工
     * @param employee
     * @return
     */
    @RequestMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee){
        log.info("新增员工，员工信息：{}", employee.toString());

        //给用户一个初始密码(采用MD5加密处理)
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        //补充新增时间等（后续自动填充不需要再设置值，统一在MyMetaObjectHandler中设置）
        /*employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //补充创建人更新人，即获取当前登陆用户的id
        Long empId = (Long) request.getSession().getAttribute("employee");
        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);*/

        employeeService.save(employee);

        return R.success("新增员工成功");
    }


    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    //list.html前端中有records、total等数据，所以泛型是MP中的Page。name是条件搜索查询用到的
    public R<Page> page(int page, int pageSize, String name){
        log.info("page = {}, pageSize = {}, name = {}", page, pageSize, name);

        //构造分页构造器：告诉MP框架想查第几页第几条
        Page pageInfo = new Page(page, pageSize);

        //构造条件构造器：动态封装查询条件
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        //添加一个过滤条件
        queryWrapper.like(
                //当name等于空时才添加
                StringUtils.isNotEmpty(name),
                //根据name字段进行查询 ::表示调用类的成员方法
                Employee::getName,
                //具体传进来的值
                name);
        //按照更新时间降序排序
        queryWrapper.orderByDesc( Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo, queryWrapper); //不用再return，因为MP的Page里封装好了

        return R.success(pageInfo);
    }

    //启用、禁用员工账号，本质上就是一个更新操作，也就是对status状态字段进行操作在Controller中创建update方法，此方法是一个通用的修改员工信息的方法，对员工进行编辑时也可以复用该方法
    /**
     * 根据id修改员工信息（更新status）
     * @param request
     * @param employee
     * @return
     */
    @PutMapping
    //RequestBody传来的JSON数据放入到响应体中
    public R<String> updateById(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());

        //获取当前线程id名
        long id = Thread.currentThread().getId();
        log.info("线程id为：{}", id);


        //获得当前登录用户的id(设置自动填充字段后不需要再设置)
        /*Long empId = (Long) request.getSession().getAttribute("employee");

        //设置修改人和修改时间
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(empId);*/

        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }


    //在开发编辑员工信息代码之前需要梳理一下操作过程和对应的程序的执行流程:
    //1、点击编辑按钮时，页面跳转到add.html，并在url中携带参数[员工id]2、在add.html页面获取url中的参数[员工id]
    //3、发送ajax请求，请求服务端，同时提交员工id参数
    //4、服务端接收请求，根据员工id查询员工信息，将员工信息以json形式响应给页面
    //5、页面接收服务端响应的json数据，通过VUE的数据绑定进行员工信息回显
    //6、点击保存按钮，发送ajax请求，将页面中的员工信息以json方式提交给服务端
    //7、服务端接收员工信息，并进行处理，完成后给页面响应
    //8、页面接收到服务端响应信息后进行相应处理
    //注意: add.html页面为公共页面，新增员工和编辑员工都是在此页面操作

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}") //http://localhost:8080/employee/1589486943828303874用来获取employee后面的id
    //@PathVariable是用来对指定请求的URL路径里面的变量
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息...");
        Employee employee = employeeService.getById(id);
        if (employee != null) {
            return R.success(employee);
        }
        return R.error("没有查询到对应员工信息");
    }
}
