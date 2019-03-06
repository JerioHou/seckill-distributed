package cn.jerio.pay.service;

import cn.jeiro.pay.api.AliPayApi;
import cn.jerio.oder.service.OrderService;
import cn.jerio.pay.config.AliPayConfig;
import cn.jerio.pay.util.BigDecimalUtil;
import cn.jerio.pojo.AliPreResult;
import cn.jerio.pojo.Bizcontent;
import cn.jerio.pojo.OrderInfo;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;

import java.math.BigDecimal;

/**
 * Created by Jerio on 2019/03/06
 */
@Service
public class AliPayService implements AliPayApi {

    private static AlipayClient alipayClient;

    static {
        AliPayConfig.init("zfbinfo.properties");


        alipayClient = new DefaultAlipayClient(AliPayConfig.getOpenApiDomain(),AliPayConfig.getAppid(),AliPayConfig.getPrivateKey(),
                "json","utf-8",AliPayConfig.getAlipayPublicKey(),AliPayConfig.getSignType());
    }

    @Reference
    private OrderService orderService;

    @Override
    public AliPreResult precreate(Long orderId) {
        AliPreResult aliPreResult = new AliPreResult();
        OrderInfo orderInfo = orderService.getOrderById(orderId);
        if (orderInfo.getStatus()!=0){
            aliPreResult.setCode("-1");
            aliPreResult.setMsg("请勿重复支付");
            return aliPreResult;
        }
        Double goodsPrice = orderInfo.getGoodsPrice();
        Integer goodsCount = orderInfo.getGoodsCount();
        BigDecimal totalAmount = BigDecimalUtil.mul(Double.valueOf(goodsCount.toString()), goodsPrice);
        Bizcontent bizContent = new Bizcontent();
        bizContent.setOut_trade_no(orderInfo.getId());
        bizContent.setSubject(orderInfo.getGoodsName());
        bizContent.setTotal_amount(totalAmount.toString());

        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setNotifyUrl("");
        request.setBizContent(JSON.toJSONString(bizContent));
        //通过alipayClient调用API，获得对应的response类
        AlipayTradePrecreateResponse response = null;
        try {
            response = alipayClient.execute(request);
            aliPreResult.setCode(response.getCode());
            aliPreResult.setMsg(response.getMsg());
            aliPreResult.setOut_trade_no(response.getOutTradeNo());
            aliPreResult.setQr_code(response.getQrCode());
        } catch (AlipayApiException e) {
            e.printStackTrace();
            aliPreResult.setCode("-2");
            aliPreResult.setMsg("请求预创建接口失败");
        }
        System.out.println(aliPreResult);
        return aliPreResult;
    }
}
