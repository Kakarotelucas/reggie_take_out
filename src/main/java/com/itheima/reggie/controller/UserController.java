package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 发送手机短信验证码
     * @param user
     * @return 请求网址：http://localhost:8080/user/sendMsg
     *   请求值：{
     *   "phone": "15581470333",
     *   "code": "555"  就是验证码
     * }
     */
    @PostMapping("/sendMsg")
    //@RequestBody主要用来接收前端传递给后端的json字符串中的数据的(请求体中的数据的)
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //1、获取手机号
        String phone = user.getPhone();

        //判断手机号不为空再生成验证码
        if(StringUtils.isNotEmpty(phone)){
            //2、调用ValidateCodeUtils工具类生成随机的6位验证码
            String code = ValidateCodeUtils.generateValidateCode(6).toString();

            //通过控制台查看生成的验证码
            log.info("code={}",code);

            //3、调用SMSUtils工具类阿里云提供的短信服务API完成发送短信
            //SMSUtils.sendMessage("瑞吉外卖","",phone,code);

            //4、需要将生成的验证码保存到Session用于比对用户输入的验证码是否正确
            session.setAttribute(phone,code);

            return R.success("手机验证码短信发送成功");
        }

        return R.error("短信发送失败");
    }

    /**
     * 移动端用户登录
     * @param map
     * @param session
     * @return 请求网址:http://localhost:8080/user/login
     * 请求荷载：{
     *   "phone": "15581476523",
     *   "code": "454555"
     * } 所以使用 map集合接收
     */
    @PostMapping("/login")
    //@RequestBody主要用来接收前端传递给后端的json字符串中的数据的(请求体中的数据的)
    public R<User> login(@RequestBody Map map, HttpSession session){
        log.info(map.toString());

        //1、获取手机号
        String phone = map.get("phone").toString();

        //2、获取验证码
        String code = map.get("code").toString();

        //3、从Session中获取保存的验证码
        Object codeInSession = session.getAttribute(phone);

        //4、进行验证码的比对（页面提交的验证码和Session中保存的验证码比对）
        if(codeInSession != null && codeInSession.equals(code)){

            //5、如果能够比对成功，说明登录成功
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);

            User user = userService.getOne(queryWrapper);

            //6、登录后判断当前手机号对应的用户是否为新用户，如果是新用户就自动完成注册
            if(user == null){
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);//正常使用
                userService.save(user);
            }

            //登陆成功后需要放一份session，不然会被过滤器过滤无法进入登录后的界面
            session.setAttribute("user",user.getId());
            //返回老用户或者新用户信息
            return R.success(user);
        }
        return R.error("登录失败");
    }

}
