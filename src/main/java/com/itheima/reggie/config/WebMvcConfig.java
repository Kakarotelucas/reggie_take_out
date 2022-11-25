package com.itheima.reggie.config;

import com.itheima.reggie.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;


/**
 * @Date: 2022/11/3 12:34
 * @Auther: cold
 * @Description: 设置静态资源映射，用于放行静态资源，放在static目录下则不用映射
 */
//用于书写日志
@Slf4j
//用于继承WebMvcConfigurationSupport
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始进行静态资源映射");
        //要拦截的资源
        registry.addResourceHandler("/backend/**")
                //要映射的地址classpath:相当于resources目录，注意是/backend/ ，后面的/不要漏写
                .addResourceLocations("classpath:/backend/");

        registry.addResourceHandler("/front/**")
                //要映射的地址classpath:相当于resources目录
                .addResourceLocations("classpath:/front/");
    }

    /**
     * 通过继承extendMessageConverters扩展MVC框架的消息转化器
     * 从而将Long类型的id转换成String类型可以解决JS中Long类型数据不准确，根据id禁用员工时id不匹配的问题
     * @param converters
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器...");

        //创建消息转换器对象：将Controller里方法的返回结果转成相应的JSON响应到页面
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();

        //设置对象转换器，底层使用Jackson将Java对象转换成JSON
        messageConverter.setObjectMapper(new JacksonObjectMapper());

        //将上面的消息转换器对象追加到MVC框架的转换器集合中，下列意为将自定义的转换器放在最前面优先使用
        converters.add(0, messageConverter);
    }
}
