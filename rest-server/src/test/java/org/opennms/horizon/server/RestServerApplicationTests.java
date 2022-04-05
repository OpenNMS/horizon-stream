package org.opennms.horizon.server;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opennms.horizon.server.controller.NodeController;
import org.opennms.horizon.server.dao.NodeRepository;
import org.opennms.horizon.server.service.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class RestServerApplicationTests {

	@Autowired
	NodeService nodeService;
	@Autowired
	NodeRepository nodeRepository;
	@Autowired
	NodeController nodeController;

	@Test
	void contextLoads() {
		assertThat(nodeService).isNotNull();
		assertThat(nodeRepository).isNotNull();
		assertThat(nodeController).isNotNull();
	}

}
