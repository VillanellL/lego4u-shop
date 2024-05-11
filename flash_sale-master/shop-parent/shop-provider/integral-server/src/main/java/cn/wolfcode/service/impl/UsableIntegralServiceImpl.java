package cn.wolfcode.service.impl;

import cn.wolfcode.domain.OperateIntegralVo;
import cn.wolfcode.mapper.UsableIntegralMapper;
import cn.wolfcode.service.IUsableIntegralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by lanxw
 */
@Service
public class UsableIntegralServiceImpl implements IUsableIntegralService {
    @Autowired
    private UsableIntegralMapper usableIntegralMapper;

    @Override
    @Transactional
    public Boolean pay(OperateIntegralVo vo) {
        int effectCount = usableIntegralMapper.substractIntegral(vo.getPhone(), vo.getValue());
        return effectCount>0;
    }

    @Override
    public void refund(OperateIntegralVo vo) {
        usableIntegralMapper.incrIntegral(vo.getPhone(),vo.getValue());
    }
}
