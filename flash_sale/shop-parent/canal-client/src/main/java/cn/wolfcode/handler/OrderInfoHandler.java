package cn.wolfcode.handler;

import cn.wolfcode.domain.OrderInfo;
import cn.wolfcode.mq.MQConstant;
import cn.wolfcode.redis.SeckillRedisKey;
import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

@Component
@CanalTable("t_order_info")
public class OrderInfoHandler implements EntryHandler<OrderInfo> {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Override
    public void insert(OrderInfo orderInfo) {
        //往Redis中设置Key,用户重复下单判断. TODO -->canal实现数据库缓存同步问题
        String orderSetKey = SeckillRedisKey.SECKILL_ORDER_SET.getRealKey(orderInfo.getSeckillId().toString());
        redisTemplate.opsForSet().add(orderSetKey,orderInfo.getPhone());
    }

    @Override
    public void update(OrderInfo before, OrderInfo after) {
        if(OrderInfo.STATUS_UNPAID.equals(before.getStatus()) && OrderInfo.STATUS_TIMEOUT.equals(after.getStatus())){
            //增加Redis库存 ===>涉及到MySQL和Redis同步的问题，使用Canal TODO
            String stockCountKey = SeckillRedisKey.SECKILL_PRODUCT_STOCK.getRealKey(after.getSeckillId().toString());
            //说明创建订单的时候出现异常，Redis库存的回补,设置本地标识为false
            redisTemplate.opsForValue().increment(stockCountKey);
            //发送消息到MQ中.
            rocketMQTemplate.syncSend(MQConstant.STOCK_OVER_FLAG_TOPIC,after.getSeckillId());
        }
    }
}
