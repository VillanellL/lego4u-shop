package cn.wolfcode.web.feign;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.PayVo;
import cn.wolfcode.domain.RefundVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Created by wolfcode-lanxw
 */
@FeignClient(name = "pay-service",path = "/alipay",fallback = PayFeignFallback.class)
public interface PayFeignApi {
    //http://pay-service/alipay/pay
    @RequestMapping("/pay")
    Result<String> pay(@RequestBody PayVo vo);
    //http://pay-service/alipay/rsaCheckV1
    @RequestMapping("/rsaCheckV1")
    Result<Boolean> rsaCheckV1(@RequestParam Map<String,String> params);
    //http://pay-service/alipay/refund
    @RequestMapping("/refund")
    Result<Boolean> refund(@RequestBody RefundVo vo);
}
