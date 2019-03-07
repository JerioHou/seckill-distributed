package cn.jeiro.pay.api;

import cn.jerio.pojo.AliPreResult;

import java.util.Map;

/**
 * Created by Jerio on 2019/03/06
 */
public interface AliPayApi {
    AliPreResult precreate(Long orderId);

    boolean checkCallbackParams(Map<String,String> params);
    boolean doCallback(Map<String,String> params);
}
