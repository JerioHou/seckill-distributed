package cn.jerio.order.zk;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by Jerio on 2019/3/7
 */
@Data
@Component
@ConfigurationProperties(prefix = "curator")
public class ZkProperties {
    private int retryCount;

    private int elapsedTimeMs;

    private String connectString;

    private int sessionTimeoutMs;

    private int connectionTimeoutMs;

}
