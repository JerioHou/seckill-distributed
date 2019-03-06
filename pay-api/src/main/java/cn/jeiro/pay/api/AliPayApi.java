package cn.jeiro.pay.api;

import cn.jerio.pojo.AliPreResult;

/**
 * Created by Jerio on 2019/03/06
 */
public interface AliPayApi {
    AliPreResult precreate(Long orderId);
}
