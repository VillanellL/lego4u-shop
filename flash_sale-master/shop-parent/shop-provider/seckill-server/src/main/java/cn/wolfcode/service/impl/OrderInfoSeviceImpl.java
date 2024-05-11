package cn.wolfcode.service.impl;

import cn.wolfcode.common.exception.BusinessException;
import cn.wolfcode.common.web.CommonCodeMsg;
import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.*;
import cn.wolfcode.mapper.OrderInfoMapper;
import cn.wolfcode.mapper.PayLogMapper;
import cn.wolfcode.mapper.RefundLogMapper;
import cn.wolfcode.mq.MQConstant;
import cn.wolfcode.mq.OrderMessage;
import cn.wolfcode.redis.SeckillRedisKey;
import cn.wolfcode.service.IOrderInfoService;
import cn.wolfcode.service.ISeckillProductService;
import cn.wolfcode.util.IdGenerateUtil;
import cn.wolfcode.web.feign.IntegralFeignApi;
import cn.wolfcode.web.feign.PayFeignApi;
import cn.wolfcode.web.msg.SeckillCodeMsg;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.SneakyThrows;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by wolfcode-lanxw
 */
@Service
public class OrderInfoSeviceImpl implements IOrderInfoService {
    @Autowired
    @Lazy
    private IOrderInfoService orderInfoService;
    @Autowired
    private ISeckillProductService seckillProductService;
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private PayLogMapper payLogMapper;
    @Autowired
    private RefundLogMapper refundLogMapper;
    public static ConcurrentHashMap<Long,Boolean> stockCountOverFlagMap = new ConcurrentHashMap<>();
    @Override
    public void doSeckill(Long seckillId, String phone, String token) {
        //判断本地标识
        Boolean stockCountOverFlag = stockCountOverFlagMap.get(seckillId);
        if(stockCountOverFlag!=null && stockCountOverFlag){
            throw new BusinessException(SeckillCodeMsg.SECKILL_STOCK_OVER);
        }
        SeckillProduct seckillProduct = seckillProductService.find(seckillId);
        //1.请求的时间控制判断，必须要抢购时间之内进行抢购才可以
       /* boolean legalTime = DateUtil.isLegalTime(seckillProduct.getStartDate(), seckillProduct.getTime());
        if(!legalTime){
            throw new BusinessException(CommonCodeMsg.ILLEGAL_OPERATION);
        }*/
        //2.一个用户只能抢购该场次一个的秒杀商品
        String orderSetKey = SeckillRedisKey.SECKILL_ORDER_SET.getRealKey(seckillId.toString());
        if(redisTemplate.opsForSet().isMember(orderSetKey,phone)){
            //判断Redis中是否存在当前用户手机号码
            throw new BusinessException(SeckillCodeMsg.REPEAT_SECKILL);
        }
        //3.不要出现超卖的情况，不要出现少卖的情况
        String stockCountKey = SeckillRedisKey.SECKILL_PRODUCT_STOCK.getRealKey(seckillId.toString());
        Long remainCount = redisTemplate.opsForValue().decrement(stockCountKey);
        if(remainCount<0){
            //保持Redis的库存为0，用户后续异常的情况下进行回补操作
            //存在问题就是: Redis递减完之后，进行Redis库存+1操作，保证Redis库存维持0的位置. 每次都是发送2次的Redis?
            redisTemplate.opsForValue().increment(stockCountKey);
            stockCountOverFlagMap.put(seckillId,true);
            throw new BusinessException(SeckillCodeMsg.SECKILL_STOCK_OVER);
        }
        //发送消息
        OrderMessage msg = new OrderMessage();
        msg.setSeckillId(seckillId);
        msg.setUserPhone(phone);
        msg.setToken(token);
        SendResult result = rocketMQTemplate.syncSend(MQConstant.ORDER_CREATE_TOPIC, msg);
        if(!SendStatus.SEND_OK.equals(result.getSendStatus())){
            redisTemplate.opsForValue().increment(stockCountKey);
            throw new BusinessException(SeckillCodeMsg.SECKILL_ERROR);
        }
        /*int effectCount = seckillProductService.decrStockCount(seckillId);
        if(effectCount==0){
            //通过扣减库存的SQL返回的受影响行数判断，是否还有库存，如果影响行数为0,说明数据库库存已经扣完，抛出异常，通知用户秒杀商品已经被抢购完毕
            throw new BusinessException(SeckillCodeMsg.SECKILL_STOCK_OVER);
        }
        try{
            String orderNo = createOrder(seckillId,phone);
        }catch (Exception e){
            //说明创建订单的时候出现异常，Redis库存的回补
            redisTemplate.opsForValue().increment(stockCountKey);
            throw e;
        }*/
        /*
        ---------------原子性操作---------------------------
        4.扣减商品的库存数量
        5.创建秒杀的订单
        ------------------------------------------*/
    }

    /**
     * 创建订单逻辑
     * @param seckillId
     * @param phone
     * @return
     */
    private String createOrder(Long seckillId, String phone) {
        SeckillProduct seckillProduct = seckillProductService.find(seckillId);
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setSeckillId(seckillId);//秒杀ID
        orderInfo.setSeckillDate(seckillProduct.getStartDate());//秒杀日期
        orderInfo.setSeckillPrice(seckillProduct.getSeckillPrice());//秒杀价格
        orderInfo.setSeckillTime(seckillProduct.getTime());//秒杀场次
        orderInfo.setProductPrice(seckillProduct.getProductPrice());//商品原价
        orderInfo.setProductImg(seckillProduct.getProductImg());//商品图片
        orderInfo.setPhone(phone);//用户手机
        orderInfo.setIntegral(seckillProduct.getIntegral());//所需积分
        orderInfo.setCreateDate(new Date());//订单创建时间
        orderInfo.setProductName(seckillProduct.getProductName());//商品名称
        orderInfo.setOrderNo(IdGenerateUtil.get().nextId()+"");//订单号
        orderInfoMapper.insert(orderInfo);
        return orderInfo.getOrderNo();
    }

    @Override
    public OrderInfo find(String orderNo) {
        return orderInfoMapper.find(orderNo);
    }

    @Override
    @Transactional
    public String createSeckillOrder(Long seckillId, String userPhone) {
        int effectCount = seckillProductService.decrStockCount(seckillId);
        if(effectCount==0){
            //通过扣减库存的SQL返回的受影响行数判断，是否还有库存，如果影响行数为0,说明数据库库存已经扣完，抛出异常，通知用户秒杀商品已经被抢购完毕
            throw new BusinessException(SeckillCodeMsg.SECKILL_STOCK_OVER);
        }
        String orderNo = null;
        try{
                orderNo = createOrder(seckillId,userPhone);
        }catch (Exception e){
            String stockCountKey = SeckillRedisKey.SECKILL_PRODUCT_STOCK.getRealKey(seckillId.toString());
            //说明创建订单的时候出现异常，Redis库存的回补,设置本地标识为false
            redisTemplate.opsForValue().increment(stockCountKey);
            //发送消息到MQ中.
            rocketMQTemplate.syncSend(MQConstant.STOCK_OVER_FLAG_TOPIC,seckillId);
            //OrderInfoSeviceImpl.stockCountOverFlagMap.put(seckillId,false);
            throw e;
        }
        return orderNo;
    }

    @Override
    @Transactional
    public void orderPayTimeoutCheck(String orderNo) {
        OrderInfo orderInfo = orderInfoMapper.find(orderNo);
        if(OrderInfo.STATUS_UNPAID.equals(orderInfo.getStatus())){
            //订单处于未支付
            //修改订单状态
            int effectCount = orderInfoMapper.changeOrderStatus(orderNo, OrderInfo.STATUS_TIMEOUT, OrderInfo.STATUS_UNPAID);
            if(effectCount==0){
                //说明该订单已经被其他线程提前修改了.
                return;
            }
            //增加数据库的库存
            seckillProductService.incrStockCount(orderInfo.getSeckillId());

        }
    }
    @Autowired
    private PayFeignApi payFeignApi;
    @Value("${pay.notifyUrl}")
    private String notifyUrl;
    @Value("${pay.returnUrl}")
    private String returnUrl;
    @Override
    public String pay(String orderNo) {
        OrderInfo orderInfo = orderInfoMapper.find(orderNo);
        if(orderInfo==null){
            throw new BusinessException(CommonCodeMsg.ILLEGAL_OPERATION);
        }
        if(!OrderInfo.STATUS_UNPAID.equals(orderInfo.getStatus())){
            return "";
        }
        PayVo vo = new PayVo();
        vo.setOutTradeNo(orderNo);//订单号
        vo.setTotalAmount(orderInfo.getSeckillPrice()+"");//支付金额
        vo.setSubject(orderInfo.getProductName());//订单名称
        vo.setBody(orderInfo.getProductName());//订单描述
        vo.setReturnUrl(returnUrl);//同步回调地址
        vo.setNotifyUrl(notifyUrl);//异步回调地址
        Result<String> result = payFeignApi.pay(vo);
        /**
         * 情况1: result==null，说明支付服务不可用，走了降级方法
         * 情况2: result!=null,code！=200 说明支付服务可用，但是调用内部报错了，统一异常处理返回Result(code=500xxx)
         * 情况3: result!=null,code==200 说明服务正常调用，正常返回
         */
        if(result==null || result.hasError()){
            throw new BusinessException(SeckillCodeMsg.PAY_SERVER_ERROR);
        }
        String html = result.getData();
        return html;
    }

    @Override
    public void orderNotify(Map<String, String> params) {
        //1.进行验签操作
        Result<Boolean> result = payFeignApi.rsaCheckV1(params);
        if(result==null || result.hasError() || !result.getData()){
            throw new BusinessException(SeckillCodeMsg.PAY_SERVER_ERROR);
        }
        //2.验签成功，执行业务逻辑代码
        String orderNo = params.get("out_trade_no");
        //把订单状态修改成已支付状态
        int effectCount = orderInfoMapper.changeOrderToPayStatus(orderNo, OrderInfo.STATUS_ACCOUNT_PAID, OrderInfo.STATUS_UNPAID, OrderInfo.PAYTYPE_ONLINE);
        if(effectCount==0){
            //说明支付回调的时候，其他线程已经将订单状态修改了，已经执行其他的业务逻辑. 走退款的逻辑
            //插入数据表中，客服审核通过之后，才对该订单进行退款的操作.
            return;
        }
        //其他逻辑....通知仓库出库，短信通知.
    }

    @Override
    public String orderReturn(Map<String, String> params) {
        Result<Boolean> result = payFeignApi.rsaCheckV1(params);
        if(result==null || result.hasError() || !result.getData()){
            throw new BusinessException(SeckillCodeMsg.PAY_SERVER_ERROR);
        }
        String orderNo = params.get("out_trade_no");
        return orderNo;
    }

    @Override
    @Transactional
    public void refund(String orderNo) {
        OrderInfo orderInfo = orderInfoMapper.find(orderNo);
        if(orderInfo==null){
            throw new BusinessException(CommonCodeMsg.ILLEGAL_OPERATION);
        }
        if(!OrderInfo.STATUS_ACCOUNT_PAID.equals(orderInfo.getStatus())){
            return;
        }
        if(OrderInfo.PAYTYPE_ONLINE.equals(orderInfo.getPayType())){
            //在线支付，调用支付宝进行退款
            this.refundOnline(orderInfo);
        }else{
            //积分支付，调用积分服务进行退款
            orderInfoService.refundByIntegral(orderInfo);
        }
    }

    @SneakyThrows
    @GlobalTransactional
    public void refundByIntegral(OrderInfo orderInfo) {
        //- 插入退款记录(订单号作为唯一主键)
        RefundLog log = new RefundLog();
        log.setOrderNo(orderInfo.getOrderNo());//订单号
        log.setRefundAmount(orderInfo.getIntegral());//积分数量
        log.setRefundReason("不想要");
        log.setRefundTime(new Date());
        try{
            refundLogMapper.insert(log);
        }catch(Exception e){
            return;
        }
        //- 远程调用积分服务，给指定用户增加积分
        OperateIntegralVo vo = new OperateIntegralVo();
        vo.setPhone(orderInfo.getPhone());
        vo.setValue(orderInfo.getIntegral());
        Result<String> result = integralFeignApi.refund(vo);
        if(result==null ||result.hasError()){
            throw new BusinessException(SeckillCodeMsg.INTEGRAL_SERVER_ERROR);
        }
        TimeUnit.SECONDS.sleep(10);
        int i = 1/0;
        //- 修改订单状态，改成已退款
        int effectCount = orderInfoMapper.changeOrderStatus(orderInfo.getOrderNo(), OrderInfo.STATUS_REFUND, OrderInfo.STATUS_ACCOUNT_PAID);
        if(effectCount==0){
            //人工处理
        }

    }

    private void refundOnline(OrderInfo orderInfo) {
        //调用支付服务进行退款
        RefundVo vo = new RefundVo();
        vo.setOutTradeNo(orderInfo.getOrderNo());//订单号
        vo.setRefundAmount(orderInfo.getSeckillPrice().toString());//退款金额(需要支付金额一致)
        vo.setRefundReason("不想要了");
        Result<Boolean> result = payFeignApi.refund(vo);
        if(result==null || result.hasError() || !result.getData()){
            throw new BusinessException(SeckillCodeMsg.PAY_SERVER_ERROR);
        }
        //修改订单状态，改成已退款状态
        int effectCount = orderInfoMapper.changeOrderStatus(orderInfo.getOrderNo(), OrderInfo.STATUS_REFUND, OrderInfo.STATUS_ACCOUNT_PAID);
        if(effectCount==0){
            //如果出现这样的情况，需要手工处理.
        }
    }
    @Autowired
    private IntegralFeignApi integralFeignApi;
    @SneakyThrows
    @Override
    @GlobalTransactional
    public void payByIntegral(String orderNo) {
        OrderInfo orderInfo = orderInfoMapper.find(orderNo);
        if(orderInfo==null){
            throw new BusinessException(CommonCodeMsg.ILLEGAL_OPERATION);
        }
        if(!OrderInfo.STATUS_UNPAID.equals(orderInfo.getStatus())){
            return;
        }
        //1.插入支付日志(保证幂等性)
        PayLog log  = new PayLog();
        log.setOrderNo(orderNo);//订单号
        log.setPayTime(new Date());//支付时间
        log.setTotalAmount(orderInfo.getIntegral());//积分数量
        try{
            payLogMapper.insert(log);
        }catch(Exception e){
            return;
        }
        //2.调用积分服务,实现积分扣减操作
        OperateIntegralVo vo = new OperateIntegralVo();
        vo.setPhone(orderInfo.getPhone());//用户
        vo.setValue(orderInfo.getIntegral());//积分数量
        Result<Boolean> result = integralFeignApi.pay(vo);
        if(result==null || result.hasError()){
            throw new BusinessException(SeckillCodeMsg.INTEGRAL_SERVER_ERROR);
        }
        if(!result.getData()){
            throw new BusinessException(SeckillCodeMsg.INTEGRAL_NOT_ENOUGH);
        }
        TimeUnit.SECONDS.sleep(20);
        int i = 1/0;
        //3.修改订单状态
        int effectCount = orderInfoMapper.changeOrderToPayStatus(orderNo, OrderInfo.STATUS_ACCOUNT_PAID, OrderInfo.STATUS_UNPAID, OrderInfo.PAYTYPE_INTEGRAL);
        if(effectCount==0){
            throw new BusinessException(SeckillCodeMsg.ORDER_STATUS_CHANGE);
        }
    }
}
