package cn.wolfcode.web.controller;


import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.OrderInfo;
import cn.wolfcode.service.IOrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * Created by lanxw
 */
@RestController
@RequestMapping("/orderPay")
public class OrderPayController {
    @Autowired
    private IOrderInfoService orderInfoService;
    @RequestMapping("/pay")
    public Result<String> pay(String orderNo,Integer type){
        if(OrderInfo.PAYTYPE_ONLINE.equals(type)){
            //在线支付
            String html = orderInfoService.pay(orderNo);
            return Result.success(html);
        }else{
            //积分支付
            orderInfoService.payByIntegral(orderNo);
            return Result.success();
        }
    }
    //异步回调地址 http://ejku4n.natappfree.cc/seckill/orderPay/notifyUrl
    @RequestMapping("/notifyUrl")
    public String notifyUrl(@RequestParam Map<String,String> params){
        System.out.println("异步回调");
        orderInfoService.orderNotify(params);
        return "success";
    }
    //同步回调地址 http://ejku4n.natappfree.cc/seckill/orderPay/returnUrl
    //http://localhost/50x.html
    @Value("${pay.errorUrl}")
    private String errorUrl;
    // http://localhost/order_detail.html?orderNo=
    @Value("${pay.frontEndPayUrl}")
    private String frontEndPayUrl;
    @RequestMapping("/returnUrl")
    public void returnUrl(@RequestParam Map<String,String> params,HttpServletResponse response) throws IOException {
        System.out.println("同步回调");
        try{
            String orderNo = orderInfoService.orderReturn(params);
            response.sendRedirect(frontEndPayUrl+orderNo);
        }catch(Exception e){
            response.sendRedirect(errorUrl);
        }
    }
    @RequestMapping("/refund")
    public Result<String> refund(String orderNo){
        orderInfoService.refund(orderNo);
        return Result.success();
    }
}
