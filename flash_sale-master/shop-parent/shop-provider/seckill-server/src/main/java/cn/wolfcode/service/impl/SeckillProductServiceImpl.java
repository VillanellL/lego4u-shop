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

    @Override
    public List<SeckillProduct> queryByTime(Integer time) {
        //从Redis中查询
        String seckillProductListKey = SeckillRedisKey.SECKILL_PRODUCT_LIST.getRealKey(time.toString());
        List<String> idList = redisTemplate.opsForList().range(seckillProductListKey, 0, -1);
       /* List<SeckillProduct> seckillProductList = idList.stream().map(id->{
            String detailKey = SeckillRedisKey.SECKILL_PRODUCT_DETAIL.getRealKey(id);
            return  JSON.parseObject(redisTemplate.opsForValue().get(detailKey),SeckillProduct.class);
        }).collect(Collectors.toList());*/
       // [1,2,3,4,5]
       // ["seckillProductDetail:1","seckillProductDetail:2","seckillProductDetail:3"]
        List<String> detailKeyList = idList.stream().map(id -> SeckillRedisKey.SECKILL_PRODUCT_DETAIL.getRealKey(id)).collect(Collectors.toList());
        //["1号商品JSON字符串","2号商品JSON字符串","3号商品JSON字符串"]
        //[1号商品对象，2号商品对象，3号商品对象]
        List<SeckillProduct> seckillProductList = redisTemplate.opsForValue().multiGet(detailKeyList).stream().map(objStr -> JSON.parseObject(objStr, SeckillProduct.class)).collect(Collectors.toList());
        return seckillProductList;
    }

    @Override
    public SeckillProduct find(Long seckillId) {
        String detailKey = SeckillRedisKey.SECKILL_PRODUCT_DETAIL.getRealKey(seckillId.toString());
        return  JSON.parseObject(redisTemplate.opsForValue().get(detailKey),SeckillProduct.class);
    }

    @Override
    public int decrStockCount(Long seckillId) {
        return seckillProductMapper.decrStock(seckillId);
    }

    @Override
    public List<SeckillProduct> queryTodayData() {
        return seckillProductMapper.querySeckillProduct();
    }

    @Override
    public void incrStockCount(Long seckillId) {
        seckillProductMapper.incrStock(seckillId);
    }
}
