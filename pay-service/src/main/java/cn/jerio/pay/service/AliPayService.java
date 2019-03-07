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
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by Jerio on 2019/03/06
 */
@Service
public class AliPayService implements AliPayApi {

    private static Logger logger = LoggerFactory.getLogger(AliPayService.class);

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
        request.setNotifyUrl("http://yc2hpf.natappfree.cc/aliPay/alipay_callback");
        request.setBizContent(JSON.toJSONString(bizContent));
        //通过alipayClient调用API，获得对应的response类
        AlipayTradePrecreateResponse response = null;
        try {
            response = alipayClient.execute(request);
            aliPreResult.setCode(response.getCode());
            aliPreResult.setMsg(response.getMsg());
            aliPreResult.setOutTradeNo(response.getOutTradeNo());
            aliPreResult.setQrCode(response.getQrCode());
        } catch (AlipayApiException e) {
            e.printStackTrace();
            aliPreResult.setCode("-2");
            aliPreResult.setMsg("请求预创建接口失败");
        }
        System.out.println(aliPreResult);
        return aliPreResult;
    }

    @Override
    public boolean checkCallbackParams(Map<String, String> params) {
        try {
            return AlipaySignature.rsaCheckV2(params, AliPayConfig.getAlipayPublicKey(),"utf-8",AliPayConfig.getSignType());
        } catch (AlipayApiException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean doCallback(Map<String,String> params) {

        String totalAmount = params.get("total_amount");
        String outTradeNo = params.get("out_trade_no");
        String paymentTime = params.get("gmt_payment");
        OrderInfo orderInfo = orderService.getOrderById(Long.valueOf(outTradeNo));
        if (orderInfo == null)
            return false;
        Double goodsPrice = orderInfo.getGoodsPrice();
        Integer goodsCount = orderInfo.getGoodsCount();
        BigDecimal totalAmountDb = BigDecimalUtil.mul(Double.valueOf(goodsCount.toString()), goodsPrice);
        if (!totalAmount.equals(totalAmountDb.toEngineeringString()))
            return false;
        OrderInfo updateInfo = new OrderInfo();
        updateInfo.setId(orderInfo.getId());
        updateInfo.setStatus(1);
        SimpleDateFormat sdf =  new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
        Date payDate;
        try {
            payDate = sdf.parse(paymentTime);
        } catch (ParseException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
        updateInfo.setPayDate(payDate);
        orderService.updateOrderStatusById(updateInfo);
        return true;
    }
}
