package cn.jerio;

import cn.jerio.order.zk.ZkLock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderServiceApplicationTests {

    @Autowired
    ZkLock zkLock;

	@Test
	public void contextLoads() {

	}

}

