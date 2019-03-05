package cn.jerio.product.service;

import cn.jerio.pojo.MiaoshaUser;

/**
 * Created by Jerio on 2019/03/04
 */
public interface MiaoshaService {

    boolean checkPath(MiaoshaUser user, long goodsId, String path);

    String createMiaoshaPath(MiaoshaUser user, long goodsId);

    void sendMsg(MiaoshaUser user, long goodsId);
}
