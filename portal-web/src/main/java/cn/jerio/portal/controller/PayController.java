package cn.jerio.portal.controller;

import cn.jeiro.pay.api.AliPayApi;
import cn.jerio.pojo.AliPreResult;
import cn.jerio.portal.util.ZxingUtils;
import cn.jerio.result.Result;
import com.alibaba.dubbo.config.annotation.Reference;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.Map;


/**
 * Created by Jerio on 2019/03/06
 */
@Controller
@RequestMapping("/aliPay")
public class PayController {

    private static Logger logger = LoggerFactory.getLogger(PayController.class);
    private static String path = "/static/img/";

    @Reference
    private AliPayApi aliPayService;

    @RequestMapping("/preCreate")
    @ResponseBody
    public Result<String> preCreate(@RequestParam("orderId") Long orderId){
        AliPreResult aliPreResult = aliPayService.precreate(orderId);
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String qrPath = String.format(rootPath+path+"\\qr-%s.png", aliPreResult.getOutTradeNo());
        String qrFileName = String.format("qr-%s.png",aliPreResult.getOutTradeNo());
        ZxingUtils.getQRCodeImge(aliPreResult.getQrCode(), 256, qrPath);
        return Result.success(qrFileName);
    }

    @RequestMapping("alipay_callback")
    @ResponseBody
    public String alipayCallback(HttpServletRequest request){
        Map<String,String> params = Maps.newHashMap();
        Map requestParams = request.getParameterMap();
        for(Iterator iter = requestParams.keySet().iterator(); iter.hasNext();){
            String name = (String)iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for(int i = 0 ; i <values.length;i++){
                valueStr = (i == values.length -1)?valueStr + values[i]:valueStr + values[i]+",";
            }
            params.put(name,valueStr);
        }
        params.remove("sign_type");
        boolean callback = aliPayService.checkCallbackParams(params);
        if (!callback){
            logger.info("回调验签失败");
            return "failed";
        }
        if(aliPayService.doCallback(params))
            return "success";
        return "failed";
    }
}
