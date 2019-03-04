package cn.jerio.oder.service;

import cn.jerio.pojo.MiaoshaUser;
import cn.jerio.pojo.OrderInfo;
import cn.jerio.vo.GoodsVo;

import java.awt.image.BufferedImage;

/**
 * Created by Jerio on 2019/03/04
 */
public interface MiaoshaService {

    OrderInfo miaosha(MiaoshaUser user, GoodsVo goods);

    BufferedImage createVerifyCode(MiaoshaUser user, long goodsId);

    boolean checkVerifyCode(MiaoshaUser user, long goodsId, int verifyCode);

    boolean checkPath(MiaoshaUser user, long goodsId, String path);

    String createMiaoshaPath(MiaoshaUser user, long goodsId);

    long getMiaoshaResult(Long userId, long goodsId);

    void sendMsg(MiaoshaUser user, long goodsId);
}
