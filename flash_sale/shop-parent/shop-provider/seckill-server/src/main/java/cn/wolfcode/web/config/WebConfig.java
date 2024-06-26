package cn.wolfcode.web.config;

import cn.wolfcode.web.interceptor.RequireLoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Created by lanxw
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private RequireLoginInterceptor requireLoginInterceptor;
    @Autowired
    private UserPhoneArgumentResolver userPhoneArgumentResolver;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requireLoginInterceptor)
                .addPathPatterns("/**");
    }
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userPhoneArgumentResolver);
    }
}
