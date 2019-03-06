package cn.jerio;

import cn.jerio.pay.service.AliPayService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PayServiceApplicationTests {

    @Autowired
    AliPayService aliPayService ;

	@Test
	public void contextLoads() {
        aliPayService.precreate(1L);
    }

}

