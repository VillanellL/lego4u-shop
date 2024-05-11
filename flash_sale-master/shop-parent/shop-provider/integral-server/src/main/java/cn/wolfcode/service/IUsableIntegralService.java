package cn.wolfcode.service;

import cn.wolfcode.domain.OperateIntegralVo;

/**
 * Created by lanxw
 */
public interface IUsableIntegralService {
    /**
     * 积分支付，扣减指定用户的积分
     * @param vo
     * @return
     */
    Boolean pay(OperateIntegralVo vo);

    /**
     * 积分退款，给指定用户增加积分
     * @param vo
     */
    void refund(OperateIntegralVo vo);
}
