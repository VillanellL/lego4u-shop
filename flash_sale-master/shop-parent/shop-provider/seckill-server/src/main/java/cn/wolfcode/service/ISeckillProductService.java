package cn.wolfcode.service;

import cn.wolfcode.domain.SeckillProduct;

import java.util.List;

/**
 * Created by lanxw
 */
public interface ISeckillProductService {
    List<SeckillProduct> queryByTime(Integer time);
    SeckillProduct find(Long seckillId);

    /**
     * 扣减商品库存数量
     * @param seckillId
     */
    int decrStockCount(Long seckillId);

    /**
     * 查询当天秒杀商品的数据集合
     * @return
     */
    List<SeckillProduct> queryTodayData();

    /**
     * 给指定的商品增加库存
     * @param seckillId
     */
    void incrStockCount(Long seckillId);
}
