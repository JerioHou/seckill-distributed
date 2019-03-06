package cn.jerio.portal.controller;

import cn.jeiro.pay.api.AliPayApi;
import cn.jerio.pojo.AliPreResult;
import cn.jerio.result.Result;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Jerio on 2019/03/06
 */
@Controller
@RequestMapping("/aliPay")
public class PayController {

    @Reference
    private AliPayApi aliPayService;

    @RequestMapping("/preCreate")
    @ResponseBody
    public Result<AliPreResult> preCreate(@RequestParam("orderId") Long orderId){
        AliPreResult aliPreResult = aliPayService.precreate(1L);

        return Result.success(aliPreResult);
    }
}
