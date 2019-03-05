package cn.jerio.order.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by Jerio on 2019/03/05
 */
@Component
public class OrderCloseTask {


    @Scheduled(cron = "")
    public void closeOrder(){

    }
}
