package cn.wolfcode.service.impl;

import cn.wolfcode.domain.OperateIntegralVo;
import cn.wolfcode.mapper.UsableIntegralMapper;
import cn.wolfcode.service.IUsableIntegralServiceTCC;
import io.seata.rm.tcc.api.BusinessActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by wolfcode-lanxw
 */
@Service
public class UsableIntegralServiceTCCImpl implements IUsableIntegralServiceTCC {
    @Autowired
    private UsableIntegralMapper usableIntegralMapper;
    @Override
    @Transactional
    public Boolean payTry(OperateIntegralVo vo, BusinessActionContext context) {
        System.out.println("执行Try逻辑");
        //进行资源预留,进行冻结金额增加
        int effectCount = usableIntegralMapper.freezeIntegral(vo.getPhone(), vo.getValue());
        return effectCount>0;
    }

    @Override
    public void payConfirm(BusinessActionContext context) {
        System.out.println("执行Confirm逻辑");
        OperateIntegralVo vo = context.getActionContext("vo", OperateIntegralVo.class);
        usableIntegralMapper.commitChange(vo.getPhone(),vo.getValue());
    }

    @Override
    public void payCancel(BusinessActionContext context) {
        System.out.println("执行Cancel逻辑");
        OperateIntegralVo vo = context.getActionContext("vo", OperateIntegralVo.class);
        usableIntegralMapper.unFreezeIntegral(vo.getPhone(),vo.getValue());
    }
}
