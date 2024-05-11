package cn.wolfcode.web.controller;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.SeckillProduct;
import cn.wolfcode.service.ISeckillProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by lanxw
 * 秒杀商品信息查询
 */
@RestController
@RequestMapping("/seckillProduct")
@Slf4j
public class SeckillProductController {
    @Autowired
    private ISeckillProductService seckillProductService;

    /**
     * 200 * 10  QPS: 650左右
     *   优化后: QPS: 850左右
     * @param time
     * @return
     */
    @RequestMapping("/queryByTime")
    public Result<List<SeckillProduct>> queryByTime(Integer time){
        List<SeckillProduct> list = seckillProductService.queryByTime(time);
        return Result.success(list);
    }

    /**
     * 200 * 10  QPS: 750左右
     *      优化后: 1600左右
     * @param seckillId
     * @return
     */
    @RequestMapping("/find")
    public Result<SeckillProduct> find(Long seckillId){
        SeckillProduct seckillProduct = seckillProductService.find(seckillId);
        return Result.success(seckillProduct);
    }
}
