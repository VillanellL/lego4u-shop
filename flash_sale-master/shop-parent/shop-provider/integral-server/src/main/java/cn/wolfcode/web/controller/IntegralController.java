package cn.wolfcode.web.controller;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.OperateIntegralVo;
import cn.wolfcode.service.IUsableIntegralService;
import cn.wolfcode.service.IUsableIntegralServiceTCC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lanxw
 */
@RestController
@RequestMapping("/integral")
public class IntegralController {
    @Autowired
    private IUsableIntegralService usableIntegralService;
    @Autowired
    private IUsableIntegralServiceTCC usableIntegralServiceTCC;
    @RequestMapping("/pay")
    public Result<Boolean> pay(@RequestBody OperateIntegralVo vo){
        //Boolean paySuccess = usableIntegralService.pay(vo);
        Boolean paySuccess = usableIntegralServiceTCC.payTry(vo,null);
        return Result.success(paySuccess);
    }
    @RequestMapping("/refund")
    public Result<String> refund(@RequestBody OperateIntegralVo vo){
        usableIntegralService.refund(vo);
        return Result.success();
    }
}
