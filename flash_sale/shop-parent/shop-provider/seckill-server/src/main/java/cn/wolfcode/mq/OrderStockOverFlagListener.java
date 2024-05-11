package cn.wolfcode.mq;

import cn.wolfcode.service.impl.OrderInfoSeviceImpl;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(consumerGroup = "stockOverFlagGroup",topic =MQConstant.STOCK_OVER_FLAG_TOPIC,messageModel = MessageModel.BROADCASTING)
public class OrderStockOverFlagListener implements RocketMQListener<Long> {
    /**
     * 库存回补时设置所有服务器的本地表示为false
     * @param seckillId
     */
    @Override
    public void onMessage(Long seckillId) {
        System.out.println("设置本地标识");
        OrderInfoSeviceImpl.stockCountOverFlagMap.put(seckillId,false);
    }
}
