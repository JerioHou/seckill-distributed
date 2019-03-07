package cn.jerio.order.task;

import cn.jerio.oder.service.OrderService;
import cn.jerio.order.zk.ZkLock;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Jerio on 2019/03/05
 */
@Slf4j
@Component
public class OrderCloseTask {

    @Autowired
    private OrderService orderService;
    @Autowired
    private ZkLock zkLock;

    private static final String LOCK_PATH = "order_close_lock";

    @Scheduled(cron = "0 */1 * * * ?")//每1分钟(每个1分钟的整数倍)
    public void closeOrder() {
        log.info("关闭订单定时任务启动");
        Date deadLine = DateUtils.addMinutes(new Date(), -30);
        String requireId = UUID.randomUUID().toString();
        if (zkLock.acquireDistributedLock(LOCK_PATH,requireId)) {
            try {
                log.info("获取分布式锁，开始关闭超时订单");
                orderService.closeOrder(deadLine);
            } finally {
                zkLock.releaseDistributedLock(LOCK_PATH,requireId);
            }
        }
    }
}
