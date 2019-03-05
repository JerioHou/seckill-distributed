package cn.jerio.order.mq;

import cn.jerio.constant.RedisKey;
import cn.jerio.oder.service.OrderService;
import cn.jerio.pojo.MiaoshaMessage;
import cn.jerio.pojo.MiaoshaOrder;
import cn.jerio.pojo.MiaoshaUser;
import cn.jerio.product.service.GoodsService;
import cn.jerio.vo.GoodsVo;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by Jerio on 2019/3/4
 */
@Component
public class RabbitmqReceiver {

    @Reference
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;

    @Resource(name = "myRedisTemplate")
    private RedisTemplate redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String MIAOSHA_QUEUE = "miaosha.queue";

    @RabbitListener(queues = MIAOSHA_QUEUE,concurrency = "5")
    public void recive(MiaoshaMessage message) {

        MiaoshaUser user = message.getUser();
        long goodsId = message.getGoodsId();

        //再次判断库存
        int stock =  Integer.parseInt(stringRedisTemplate.opsForValue().get(RedisKey.MiaoshaGoodsStock+ goodsId));
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
        orderService.miaosha(user, goods);
    }
}
