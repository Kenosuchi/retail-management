package com.retail.retailmanagement;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RetailManagementApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void livenessProbeIsPublicAndDoesNotExposeDetails() throws Exception {
		mockMvc.perform(get("/actuator/health/liveness"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(jsonPath("$.components").doesNotExist())
				.andExpect(jsonPath("$.details").doesNotExist());
	}

	@Test
	void readinessProbeIsPublicAndDoesNotExposeDetails() throws Exception {
		mockMvc.perform(get("/actuator/health/readiness"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(jsonPath("$.components").doesNotExist())
				.andExpect(jsonPath("$.details").doesNotExist());
	}

	@Test
	void rootHealthEndpointIsNotPublic() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isForbidden());
	}

	@Test
	void arbitraryApplicationRequestIsDeniedWithoutLoginRedirectOrBasicChallenge() throws Exception {
		mockMvc.perform(get("/api/example"))
				.andExpect(status().isForbidden())
				.andExpect(header().doesNotExist(HttpHeaders.LOCATION))
				.andExpect(header().doesNotExist(HttpHeaders.WWW_AUTHENTICATE));
	}

	@Test
	void nonGetProbeRequestIsDenied() throws Exception {
		mockMvc.perform(post("/actuator/health/liveness").with(csrf()))
				.andExpect(status().isForbidden());
	}

	@Test
	void loginEndpointIsNotAvailable() throws Exception {
		mockMvc.perform(get("/login"))
				.andExpect(status().isForbidden())
				.andExpect(header().doesNotExist(HttpHeaders.LOCATION));
	}

	@Test
	void sensitiveActuatorEndpointIsNotAccessible() throws Exception {
		mockMvc.perform(get("/actuator/env"))
				.andExpect(status().isForbidden());
	}

}
