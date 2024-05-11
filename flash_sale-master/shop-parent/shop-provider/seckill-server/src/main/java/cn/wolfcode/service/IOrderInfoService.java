package cn.wolfcode.service;


import cn.wolfcode.domain.OrderInfo;

import java.util.Map;

/**
 * Created by wolfcode-lanxw
 */
public interface IOrderInfoService {
    /**
     * 秒杀逻辑
     * @param seckillId
     * @param phone
     * @param token
     */
    void doSeckill(Long seckillId, String phone, String token);

    /**
     * 查询订单信息
     * @param orderNo
     * @return
     */
    OrderInfo find(String orderNo);

    /**
     * 创建秒杀订单
     * @param seckillId
     * @param userPhone
     * @return
     */
    String createSeckillOrder(Long seckillId, String userPhone);

    /**
     * 订单支付超时检查
     * @param orderNo
     */
    void orderPayTimeoutCheck(String orderNo);

    /**
     * 获取跳转到支付宝的html内容
     * @param orderNo
     * @return
     */
    String pay(String orderNo);

    /**
     * 订单异步回调逻辑
     * @param params
     */
    void orderNotify(Map<String, String> params);

    /**
     * 订单同步回调逻辑
     * @param params
     * @return
     */
    String orderReturn(Map<String, String> params);

    /**
     * 退款功能
     * @param orderNo
     */
    void refund(String orderNo);

    /**
     * 积分支付功能
     * @param orderNo
     */
    void payByIntegral(String orderNo);

    /**
     * 积分退款
     * @param orderInfo
     */
    public void refundByIntegral(OrderInfo orderInfo);
}
