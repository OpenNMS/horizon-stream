package org.opennms.horizon.server;

import org.opennms.horizon.server.dao.NodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.event.EventListener;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
@EnableCaching
public class RestServerApplication {

	@Autowired
	private NodeRepository nodeRepo;

	public static void main(String[] args) {
		SpringApplication.run(RestServerApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void testNodeRepo() {
		log.info("Application is ready");
	}
}
