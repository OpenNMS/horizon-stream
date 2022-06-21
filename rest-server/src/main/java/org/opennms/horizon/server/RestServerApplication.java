package org.opennms.horizon.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.event.EventListener;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
@EnableCaching
@OpenAPIDefinition(info = @Info(title = "Horizon Stream REST API", version = "0.1.0-SNAPSHOT", description = "REST API docs with swagger ui"),
	tags = @Tag(name = "Horizon Stream REST API", description = "Spring boot application")
)
@SecurityScheme(name = "security_auth", type = SecuritySchemeType.OAUTH2,
	flows = @OAuthFlows(password = @OAuthFlow(tokenUrl = "${springdoc.oAuthFlow.tokenUrl}"))
)
public class RestServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestServerApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void testNodeRepo() {
		log.info("Application is ready");
	}
}
