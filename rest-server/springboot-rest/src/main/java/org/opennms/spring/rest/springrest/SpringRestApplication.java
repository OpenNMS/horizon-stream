package org.opennms.spring.rest.springrest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
@Configuration
@SpringBootApplication
public class SpringRestApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringRestApplication.class, args);
	}

}
