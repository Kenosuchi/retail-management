package com.retail.retailmanagement;

import java.util.UUID;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mysql.MySQLContainer;

@TestConfiguration(proxyBeanMethods = false)
class MySqlTestConfiguration {

	@Bean
	@ServiceConnection
	MySQLContainer mysqlContainer() {
		return new MySQLContainer("mysql:8.4.11")
				.withDatabaseName("retail_management_test")
				.withUsername("retail_test")
				.withPassword(UUID.randomUUID().toString());
	}
}
