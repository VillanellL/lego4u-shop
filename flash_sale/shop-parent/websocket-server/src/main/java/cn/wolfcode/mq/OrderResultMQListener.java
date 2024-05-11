package cn.wolfcode.mq;
import cn.wolfcode.ws.OrderWsListener;
import com.alibaba.fastjson.JSON;
import lombok.SneakyThrows;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.util.concurrent.TimeUnit;

@Component
@RocketMQMessageListener(consumerGroup = "WSListener",topic = MQConstants.ORDER_RESULT_TOPIC,messageModel = MessageModel.BROADCASTING)
public class OrderResultMQListener implements RocketMQListener<OrderMQResult> {
    @SneakyThrows
    @Override
    public void onMessage(OrderMQResult message) {
        System.out.println(JSON.toJSONString(message));
        String token = message.getToken();
        int count = 0;
        do {
            Session session = OrderWsListener.clients.get(token);
            if(session != null) {
                session.getBasicRemote().sendText(JSON.toJSONString(message));
                return;
            }
            TimeUnit.MILLISECONDS.sleep(100);
            count++;
        } while(count < 2);
        System.out.println("没有找到映射关系，结束通知");
    }
}
