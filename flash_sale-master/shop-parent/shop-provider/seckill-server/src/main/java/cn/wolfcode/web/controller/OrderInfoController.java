package cn.wolfcode.web.controller;

import cn.wolfcode.common.constants.CommonConstants;
import cn.wolfcode.common.exception.BusinessException;
import cn.wolfcode.common.web.CommonCodeMsg;
import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.OrderInfo;
import cn.wolfcode.service.IOrderInfoService;
import cn.wolfcode.service.ISeckillProductService;
import cn.wolfcode.web.anno.RequireLogin;
import cn.wolfcode.web.config.UserPhone;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * Created by lanxw
 */
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderInfoController {
    @Autowired
    private IOrderInfoService orderInfoService;
    /**
     * SpringMVC中有很多的参数解析器,在真正执行业务方法之前，遍历参数解析器集合，找到合适的参数解析器，
     * 由该参数解析器，返回具体的参数，再调用控制方法.
     */
    @RequestMapping("/doSeckill")
    @RequireLogin
    public Result<String> doSeckill(Long seckillId, @UserPhone String phone,@RequestHeader(name = CommonConstants.TOKEN_NAME) String token){
        orderInfoService.doSeckill(seckillId,phone,token);
        return Result.success("进入抢购队列，请耐心等待结果");
    }
    @RequestMapping("/find")
    @RequireLogin
    public Result<OrderInfo> find(String orderNo,@UserPhone String phone){
        if(!StringUtils.hasText(orderNo)){
            throw new BusinessException(CommonCodeMsg.ILLEGAL_OPERATION);
        }
        OrderInfo orderInfo = orderInfoService.find(orderNo);
        if(orderInfo==null || !phone.equals(orderInfo.getPhone())){
            throw new BusinessException(CommonCodeMsg.ILLEGAL_OPERATION);
        }
        return Result.success(orderInfo);
    }
}
