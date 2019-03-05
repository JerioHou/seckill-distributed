package cn.jerio.product.mq;

import cn.jerio.pojo.MiaoshaMessage;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Jerio on 2019/03/04
 */
@Component
public class RabbitmqSender {
    @Autowired
    AmqpTemplate amqpTemplate;

    public void sendMiaoshaMessage(MiaoshaMessage mm) {

        amqpTemplate.convertAndSend(RabbmitmqConfig.MIAOSHA_QUEUE,mm);
    }
}
