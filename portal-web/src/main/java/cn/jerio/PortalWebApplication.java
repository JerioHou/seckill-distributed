package cn.jerio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;

@EnableHystrix
@SpringBootApplication
public class PortalWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(PortalWebApplication.class, args);
	}

}
