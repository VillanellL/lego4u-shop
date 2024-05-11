package cn.wolfcode.service.impl;

import cn.wolfcode.common.exception.BusinessException;
import cn.wolfcode.common.web.CommonCodeMsg;
import cn.wolfcode.domain.SeckillProduct;
import cn.wolfcode.mapper.SeckillProductMapper;
import cn.wolfcode.redis.SeckillRedisKey;
import cn.wolfcode.service.ISeckillProductService;
import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.Buffer;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lanxw
 */
@Service
public class SeckillProductServiceImpl implements ISeckillProductService {
    @Autowired
    private SeckillProductMapper seckillProductMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 直接从 Redis 中查询商品
     * @param time
     * @return
     */
    @Override
    public List<SeckillProduct> queryByTime(Integer time) {
        String key = SeckillRedisKey.SECKILL_PRODUCT_LIST.getRealKey(time.toString());
        // 拿到指定 key 中的所有商品信息
        List<String> idList = redisTemplate.opsForList().range(key, 0, -1);
        if(idList.isEmpty()){
            return Collections.EMPTY_LIST;
        }
        List<String> keyList = idList.stream().map(id -> SeckillRedisKey.SECKILL_PRODUCT_DETAIL.getRealKey(id)).collect(Collectors.toList());
        List<SeckillProduct> seckillProductList = redisTemplate.opsForValue().multiGet(keyList).stream().filter(objStr -> objStr != null).map(objStr -> JSON.parseObject(objStr, SeckillProduct.class)).collect(Collectors.toList());
        return seckillProductList;
    }

    @Override
    public SeckillProduct find( Long seckillId) {
        String key = SeckillRedisKey.SECKILL_PRODUCT_DETAIL.getRealKey(seckillId.toString());
        String objStr = redisTemplate.opsForValue().get(key);
        if(objStr==null){
            throw new BusinessException(CommonCodeMsg.ILLEGAL_OPERATION);
        }
        return JSON.parseObject(objStr,SeckillProduct.class);
    }

    @Override
    public int decrStockCount(Long seckillId) {
        return seckillProductMapper.decrStock(seckillId);
    }

    @Override
    public int decrStock(Long seckillId) {
        return seckillProductMapper.decrStock(seckillId);
    }

    @Override
    public void incrStockCount(Long seckillId) {
        seckillProductMapper.incrStock(seckillId);
    }

    @Override
    public List<SeckillProduct> queryTodayData() {
        return seckillProductMapper.querySeckillProduct();
    }
}
