package cn.wolfcode.web.service;

import cn.wolfcode.domain.PayVo;
import cn.wolfcode.domain.RefundVo;
import com.alipay.api.AlipayApiException;

import java.util.Map;

/**
 * Created by wolfcode-lanxw
 */
public interface IAlipayService {
    //调用SDK返回html文本
    String pay(PayVo vo) throws AlipayApiException;

    /**
     * 进行验签操作
     * @param params
     * @return
     */
    Boolean rsaCheckV1(Map<String, String> params) throws AlipayApiException;

    /**
     * 进行退款操作
     * @param vo
     * @return
     */
    Boolean refund(RefundVo vo) throws AlipayApiException;
}
