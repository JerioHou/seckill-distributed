package cn.jerio.order.mq;

import cn.jerio.constant.RedisKey;
import cn.jerio.oder.service.OrderService;
import cn.jerio.pojo.MiaoshaMessage;
import cn.jerio.pojo.MiaoshaOrder;
import cn.jerio.pojo.MiaoshaUser;
import cn.jerio.product.service.GoodsService;
import cn.jerio.product.service.MiaoshaService;
import cn.jerio.vo.GoodsVo;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by Jerio on 2019/3/4
 */
@Component
public class RabbitmqReceiver {

    @Reference
    private MiaoshaService miaoshaService;

    @Reference
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;

    @Resource
    private RedisTemplate redisTemplate;

    private static final String MIAOSHA_QUEUE = "miaosha.queue";

    @RabbitListener(queues = MIAOSHA_QUEUE,concurrency = "5")
    public void recive(String message) {

        MiaoshaMessage mm = JSON.parseObject(message, MiaoshaMessage.class);

        MiaoshaUser user = mm.getUser();
        long goodsId = mm.getGoodsId();

        //再次判断库存
        int stock = (int) redisTemplate.opsForValue().get(RedisKey.MiaoshaGoodsStock+ goodsId);
        if (stock <= 0) {
            return;
        }
        //判断是否已经秒杀到了
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            return;
        }
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        //减库存 下订单 写入秒杀订单
        miaoshaService.miaosha(user, goods);
    }
}
