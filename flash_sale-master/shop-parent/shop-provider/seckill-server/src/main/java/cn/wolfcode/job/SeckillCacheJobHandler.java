package cn.wolfcode.job;

import cn.wolfcode.domain.SeckillProduct;
import cn.wolfcode.redis.SeckillRedisKey;
import cn.wolfcode.service.ISeckillProductService;
import com.alibaba.fastjson.JSON;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by wolfcode-lanxw
 */
@Component
public class SeckillCacheJobHandler {
    @Autowired
    private ISeckillProductService seckillProductService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @XxlJob("initCache")
    public void initCache(){
        //1.查询当天的所有秒杀商品数据集合
        List<SeckillProduct> seckillProductList = seckillProductService.queryTodayData();
        //2.遍历商品数据集合，使用String类型存储到Redis中.（缓存1天时间）
       /* seckillProductList.stream().forEach((seckillProduct -> {
            //定义Redis的Key  ===> seckillProductDetail:1,seckillProductDetail:2
            String detailKey = SeckillRedisKey.SECKILL_PRODUCT_DETAIL.getRealKey(seckillProduct.getId().toString());
            redisTemplate.opsForValue().set(detailKey, JSON.toJSONString(seckillProduct),SeckillRedisKey.SECKILL_PRODUCT_DETAIL.getExpireTime(),SeckillRedisKey.SECKILL_PRODUCT_DETAIL.getUnit());
        }));*/
       // List<SeckillProduct>    ===>   Map<detailKey,对象JSON字符串>
        Map<String, String> detailKeyMap = seckillProductList.stream().collect(Collectors.toMap(
                seckillProduct -> SeckillRedisKey.SECKILL_PRODUCT_DETAIL.getRealKey(seckillProduct.getId().toString()),//key的函数
                seckillProduct -> JSON.toJSONString(seckillProduct)));//value函数
        redisTemplate.opsForValue().multiSet(detailKeyMap);
        //将数据库库存同步到Redis中  seckillProductStock:1
        Map<String, String> stockCountKeyMap = seckillProductList.stream().collect(Collectors.toMap(
                seckillProduct -> SeckillRedisKey.SECKILL_PRODUCT_STOCK.getRealKey(seckillProduct.getId().toString()),//key的函数
                seckillProduct -> seckillProduct.getStockCount().toString()));
        redisTemplate.opsForValue().multiSet(stockCountKeyMap);
        //3.把数据按照场次放入到不同的List集合中.
        //将数据按照场次进行分组的操作List<SeckillProduct> ===> Map<Integer,List<SeckillProduct>>
        Map<Integer, List<SeckillProduct>> seckillGroupByTimeList = seckillProductList.stream().collect(Collectors.groupingBy(SeckillProduct::getTime));
        //先删除昨日的商品数据集合,把当日的数据加入到集合中
        seckillGroupByTimeList.entrySet().stream().forEach(entry->{
            Integer time = entry.getKey();
            String seckillProductListKey = SeckillRedisKey.SECKILL_PRODUCT_LIST.getRealKey(time.toString());
            redisTemplate.delete(seckillProductListKey);
            List<SeckillProduct> seckillProducts = entry.getValue();
            /*seckillProducts.stream().forEach(seckillProduct -> {
                redisTemplate.opsForList().leftPush(seckillProductListKey,seckillProduct.getId().toString());
            });*/
            List<String> ids = seckillProducts.stream().map(seckillProduct -> seckillProduct.getId().toString()).collect(Collectors.toList());
            redisTemplate.opsForList().leftPushAll(seckillProductListKey,ids);
        });
    }
}
