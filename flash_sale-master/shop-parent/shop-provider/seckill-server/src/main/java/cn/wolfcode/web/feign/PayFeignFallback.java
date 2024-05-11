package cn.wolfcode.web.feign;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.PayVo;
import cn.wolfcode.domain.RefundVo;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by wolfcode-lanxw
 */
@Component
public class PayFeignFallback implements PayFeignApi {
    @Override
    public Result<String> pay(PayVo vo) {
        return null;
    }

    @Override
    public Result<Boolean> rsaCheckV1(Map<String, String> params) {
        return null;
    }

    @Override
    public Result<Boolean> refund(RefundVo vo) {
        return null;
    }
}
