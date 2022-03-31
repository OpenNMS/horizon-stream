package org.opennms.horizon.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opennms.horizon.server.model.entity.MonitoringLocation;
import org.opennms.horizon.server.model.entity.Node;
import org.opennms.horizon.server.repository.NodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class RestServerApplication {

	@Autowired
	private NodeRepository nodeRepo;

	public static void main(String[] args) {
		SpringApplication.run(RestServerApplication.class, args);
	}

	/*@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			testNodeRepo(ctx);
		};
	}*/

	@EventListener(ApplicationReadyEvent.class)
	private void testNodeRepo() {
		//NodeRepository nodeRepo = ctx.getBean(NodeRepository.class);
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
		list.forEach(n -> nodeRepo.save(n));
		Node node3 = new Node();
		node3.setLabel("Company office");
		node3.setLocation(location);
		node3.setCreateTime(new Date());
	}


}
