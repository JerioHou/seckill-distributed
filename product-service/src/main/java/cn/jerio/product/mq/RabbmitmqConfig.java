package cn.jerio.product.mq;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Created by Jerio on 2019/03/04
 */
@Component
public class RabbmitmqConfig {
    public static final String MIAOSHA_QUEUE = "miaosha.queue";


    @Bean
    public Queue miaoshaQueue(){
        return new Queue(RabbmitmqConfig.MIAOSHA_QUEUE);
    }
}
