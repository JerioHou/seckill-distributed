package cn.jerio.portal.controller;

import cn.jeiro.pay.api.AliPayApi;
import cn.jerio.pojo.AliPreResult;
import cn.jerio.portal.util.ZxingUtils;
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

    private static String path = "/static/img/";

    @Reference
    private AliPayApi aliPayService;

    @RequestMapping("/preCreate")
    @ResponseBody
    public Result<String> preCreate(@RequestParam("orderId") Long orderId){
        AliPreResult aliPreResult = aliPayService.precreate(orderId);
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String qrPath = String.format(rootPath+path+"\\qr-%s.png", aliPreResult.getOut_trade_no());
        String qrFileName = String.format("qr-%s.png",aliPreResult.getOut_trade_no());
        ZxingUtils.getQRCodeImge(aliPreResult.getQr_code(), 256, qrPath);
        return Result.success(qrFileName);
    }
}
