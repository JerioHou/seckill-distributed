package cn.jerio.order.zk;

import lombok.Data;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Jerio on 2019/3/7
 */

@Configuration
public class ZkConfig {

    @Autowired
    private ZkProperties zkProperties;

    @Bean(initMethod = "start")
    public CuratorFramework curatorFramework() {
        return CuratorFrameworkFactory.newClient(
                zkProperties.getConnectString(),
                zkProperties.getSessionTimeoutMs(),
                zkProperties.getConnectionTimeoutMs(),
                new RetryNTimes(zkProperties.getRetryCount(), zkProperties.getElapsedTimeMs()));
    }
}
