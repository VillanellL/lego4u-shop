package cn.wolfcode.web.service.impl;

import cn.wolfcode.config.AlipayConfig;
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
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AlipayServiceImpl implements IAlipayService {
    @Autowired
    private AlipayClient alipayClient;
    @Autowired
    private AlipayProperties alipayProperties;
    @Override
    public String pay(PayVo vo) throws AlipayApiException {
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        //同步回调地址
        alipayRequest.setReturnUrl(vo.getReturnUrl());
        //异步回调地址
        alipayRequest.setNotifyUrl(vo.getNotifyUrl());
        //把参数封装成JSON
        alipayRequest.setBizContent("{\"out_trade_no\":\""+ vo.getOutTradeNo() +"\","
                + "\"total_amount\":\""+ vo.getTotalAmount() +"\","
                + "\"subject\":\""+ vo.getSubject() +"\","
                + "\"body\":\""+ vo.getBody() +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");
        String html = alipayClient.pageExecute(alipayRequest).getBody();
        return html;
    }

    @Override
    public Boolean rsaCheckV1(Map<String, String> params) throws AlipayApiException {
        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayProperties.getAlipayPublicKey(), alipayProperties.getCharset(), alipayProperties.getSignType()); //调用SDK验证签名
        return signVerified;
    }

    @Override
    public Boolean refund(RefundVo vo) throws AlipayApiException {
        AlipayTradeRefundRequest alipayRequest = new AlipayTradeRefundRequest();
        alipayRequest.setBizContent("{\"out_trade_no\":\""+ vo.getOutTradeNo() +"\","
                + "\"trade_no\":\"\","
                + "\"refund_amount\":\""+ vo.getRefundAmount() +"\","
                + "\"refund_reason\":\""+ vo.getRefundReason() +"\","
                + "\"out_request_no\":\"\"}");
        AlipayTradeRefundResponse response = alipayClient.execute(alipayRequest);
        return response.isSuccess();
    }
}
