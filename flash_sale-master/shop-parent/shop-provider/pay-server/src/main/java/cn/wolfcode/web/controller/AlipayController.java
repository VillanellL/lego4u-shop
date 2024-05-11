package cn.wolfcode.web.controller;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.config.AlipayProperties;
import cn.wolfcode.domain.PayVo;
import cn.wolfcode.domain.RefundVo;
import cn.wolfcode.web.service.IAlipayService;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by lanxw
 */
@RestController
@RequestMapping("/alipay")
public class AlipayController {
    @Autowired
    private AlipayClient alipayClient;
    @Autowired
    private AlipayProperties alipayProperties;
    @Autowired
    private IAlipayService alipayService;
    @RequestMapping("/pay")
    public Result<String> pay(@RequestBody PayVo vo) throws AlipayApiException {
        String html = alipayService.pay(vo);
        return Result.success(html);
    }
    @RequestMapping("/rsaCheckV1")
    public Result<Boolean> rsaCheckV1(@RequestParam Map<String, String> params) throws AlipayApiException {
        Boolean signVerified = alipayService.rsaCheckV1(params);
        return Result.success(signVerified);
    }
    @RequestMapping("/refund")
    public Result<Boolean> refund(@RequestBody RefundVo vo) throws AlipayApiException {
        Boolean successFlag = alipayService.refund(vo);
        return Result.success(successFlag);
    }
}
