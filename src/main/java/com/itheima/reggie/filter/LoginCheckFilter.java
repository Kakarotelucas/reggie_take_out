package com.itheima.reggie.filter;


import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Date: 2022/11/4 17:16
 * @Auther: cold
 * @Description: 过滤器，用于检查用户是否完成登陆,不登陆不能进入页面后台，不然的话没登陆也可以进入页面后台
 */
//@WebFilter 用于将一个类声明为过滤器，该注解将会在部署时被容器处理，容器将根据具体的属性配置将相应的类部署为过滤器。filterName是过滤器名字 urlPatterns只要拦截的路径
@WebFilter(filterName = "LoginCheckFilter", urlPatterns = "/*")

@Slf4j //日志
public class LoginCheckFilter implements Filter {

    //用一个匹配器AntPathMatcher（支持通配符）与String[] urls = new String[]中的"/backend/**"与backed/index.html进行比较（因为二者格式不一致）
    public static final AntPathMatcher APATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //1、获取本次请求的URI
        String requestURI = request.getRequestURI();

        //{}相当于一个占位符，跟逗号一起使用可以连接后面的requestURI
        log.info("拦截到请求：{}", requestURI);
        //定义不需要处理的请求路径
        String[] urls = new String[]{
                //用户登陆、退出时都不需要处理，直接放行即可
                "/employee/login",
                "/employee/logout",
                //其他静态资源也不需要拦截
                "/backend/**",
                "/front/**",
                "/common/**",

                "/user/sendMsg",//移动端发送短信
                "/user/login" //移动端登陆
        };

        //2、判断本次请求是否需要处理:backed/index.html是否在String[] urls中，在则不需要处理
        boolean check = check(urls, requestURI);

        //3、如果不需要处理（check = true），则直接放行
        if (check == true){
            log.info("本次请求{}不需要处理", requestURI);
            filterChain.doFilter(request, response);
            //结束方法，后面的语句不执行
            return;
        }

        //4-1、判断后台页面登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("employee") != null){
            log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("employee"));

            //得到登陆后的id，用于自动填充公共字段
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request,response);
            return;
        }

        //4-2、判断移动端页面登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("user") != null){
            log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("user"));

            //得到登陆后的id，用于自动填充公共字段
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request,response);
            return;
        }

        // 5、如果未登录则返回未登录结果，在backend/js/request.js里响应拦截器处
        log.info("用户未登录");
        // 看到可以应该将R对象转换成JSON再通过输出流方式向客户端相应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;

    }

    /**
     * 路径匹配，检查本次请求是否需要放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls, String requestURI){
        for (String url : urls) {
            //遍历的每个路径与传来的requestURI进行比对
            boolean match = APATH_MATCHER.match(url, requestURI);
            //匹配上了，放行
            if(match == true){
                return true;
            }
        }
        //全循环了匹配不上不放行
        return false;
    }
}
