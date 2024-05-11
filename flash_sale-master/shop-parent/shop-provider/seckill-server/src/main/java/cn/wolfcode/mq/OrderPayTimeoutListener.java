package cn.wolfcode.mq;

import cn.wolfcode.service.IOrderInfoService;
import groovy.transform.AutoFinal;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by wolfcode-lanxw
 */
@Component
@RocketMQMessageListener(consumerGroup = "payTimeoutGroup",topic = MQConstant.ORDER_PAY_TIMEOUT_TOPIC)
public class OrderPayTimeoutListener implements RocketMQListener<String> {
   @Autowired
    private IOrderInfoService orderInfoService;
    @Override
    public void onMessage(String orderNo) {
        System.out.println("订单支付超时检查.....");
        orderInfoService.orderPayTimeoutCheck(orderNo);
    }
}
