package org.opennms.horizon.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.opennms.horizon.server.model.MonitoringLocation;
import org.opennms.horizon.server.model.Node;
import org.opennms.horizon.server.repository.NodeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RestServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestServerApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			testNodeRepo(ctx);
		};
	}

	private void testNodeRepo(ApplicationContext ctx) {
		NodeRepository nodeRepo = ctx.getBean(NodeRepository.class);
		MonitoringLocation location = new MonitoringLocation();
		location.setId("default");
		location.setMonitoringArea("Kanata Office");
		location.setGeolocation("Ottawa, Ontario Canada");
		Node node = new Node();
		node.setLocation(location);
		node.setCreateTime(new Date());
		node.setLabel("default");
		List<Node> list = new ArrayList<>();
		list.add(node);
		Node node2 = new Node();
		node2.setLocation(location);
		node2.setLabel("Home office");
		node2.setCreateTime(new Date());
		list.add(node2);
		List<Node> savedList = nodeRepo.saveAll(list);
		savedList.forEach(n -> System.out.println(n));
	}


}
