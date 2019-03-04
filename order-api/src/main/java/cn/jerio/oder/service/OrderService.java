package cn.jerio.oder.service;

import cn.jerio.pojo.MiaoshaOrder;
import cn.jerio.pojo.MiaoshaUser;
import cn.jerio.pojo.OrderInfo;
import cn.jerio.vo.GoodsVo;

/**
 * Created by Jerio on 2019/03/04
 */
public interface OrderService {

    MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(long userId, long goodsId);

    OrderInfo createOrder(MiaoshaUser user, GoodsVo goods);

    OrderInfo getOrderById(long orderId);
}
