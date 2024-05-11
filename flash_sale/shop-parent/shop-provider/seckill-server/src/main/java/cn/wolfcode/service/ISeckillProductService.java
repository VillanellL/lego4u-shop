package cn.wolfcode.service;

import cn.wolfcode.domain.SeckillProduct;

import java.util.List;

/**
 * Created by lmhz
 */
public interface ISeckillProductService {
    /**
     * 按照时间场次查询当天的秒杀商品
     * @param time
     * @return
     */
    List<SeckillProduct> queryByTime(Integer time);

    /**
     * 根据商品ID查找
     * @param seckillId
     * @return
     */
    SeckillProduct find(Long seckillId);

    /**
     * 扣减商品库存数量
     *
     * @param seckillId
     */
    int decrStockCount(Long seckillId);

    /**
     * 给指定商品增加库存
     * @param seckillId
     */
    void incrStockCount(Long seckillId);

    /**
     * 查询当天秒杀商品的数据集合
     * @return
     */
    List<SeckillProduct> queryTodayData();
}
