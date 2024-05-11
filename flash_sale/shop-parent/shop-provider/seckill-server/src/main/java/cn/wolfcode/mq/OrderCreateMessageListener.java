import cn.wolfcode.common.exception.BusinessException;
import cn.wolfcode.common.web.Result;
import cn.wolfcode.mq.MQConstant;
import cn.wolfcode.mq.OrderMQResult;
import cn.wolfcode.mq.OrderMessage;
import cn.wolfcode.service.IOrderInfoService;
import cn.wolfcode.web.msg.SeckillCodeMsg;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(consumerGroup = "orderCreateGroup",topic = MQConstant.ORDER_CREATE_TOPIC )
public class OrderCreateMessageListener implements RocketMQListener<OrderMessage> {
    @Autowired
    private IOrderInfoService orderInfoService;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 下单操作：修改 mysql 数据库中的库存
     * @param message
     */
    @Override
    public void onMessage(OrderMessage message) {
        // 通过WebSocket通知结果
        OrderMQResult orderMQResult = new OrderMQResult();
        orderMQResult.setToken(message.getToken());
        try{
            String orderNo = orderInfoService.createSeckillOrder(message.getSeckillId(),message.getUserPhone());
            orderMQResult.setCode(Result.SUCCESS_CODE);
            orderMQResult.setOrderNo(orderNo);
            // 成功之后，发送延时消息
            Message<String> delayMsg = MessageBuilder.withPayload(orderNo).build();
            rocketMQTemplate.syncSend(MQConstant.ORDER_PAY_TIMEOUT_TOPIC,delayMsg,MQConstant.MESSAGE_SEND_TIMEOUT,MQConstant.ORDER_PAY_TIMEOUT_DELAY_LEVEL);
        }catch (Exception e){
            // 秒杀失败结果的通知
            orderMQResult.setCode(SeckillCodeMsg.SECKILL_ERROR.getCode());
            orderMQResult.setMsg(SeckillCodeMsg.SECKILL_ERROR.getMsg());
        }
        rocketMQTemplate.syncSend(MQConstant.ORDER_RESULT_TOPIC,orderMQResult);
    }
}
