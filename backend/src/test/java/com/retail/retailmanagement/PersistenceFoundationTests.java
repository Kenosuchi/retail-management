package com.retail.retailmanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.sql.DataSource;

import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewInterceptor;

@SpringBootTest
@AutoConfigureMockMvc
@Import(MySqlTestConfiguration.class)
class PersistenceFoundationTests {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private Flyway flyway;

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	void connectsToMySql84WithANonRootAccount() throws Exception {
		try (var connection = dataSource.getConnection()) {
			var metadata = connection.getMetaData();
			assertEquals("MySQL", metadata.getDatabaseProductName());
			assertEquals(8, metadata.getDatabaseMajorVersion());
			assertEquals(4, metadata.getDatabaseMinorVersion());
			assertEquals("retail_management_test", connection.getCatalog());
			assertEquals("retail_test", metadata.getUserName().split("@", 2)[0]);
		}
	}

	@Test
	void flywayRecordsSuccessfulSmokeMigration() throws Exception {
		assertTrue(flyway.validateWithResult().validationSuccessful);
		try (var connection = dataSource.getConnection();
				var statement = connection.prepareStatement(
						"SELECT script, success FROM flyway_schema_history WHERE version = ?")) {
			statement.setString(1, "0");
			try (var rows = statement.executeQuery()) {
				assertTrue(rows.next(), "Flyway must apply the test migration at startup");
				assertEquals("V0__create_persistence_smoke_table.sql", rows.getString("script"));
				assertTrue(rows.getBoolean("success"));
				assertFalse(rows.next(), "The version must be recorded exactly once");
			}
		}
	}

	@Test
	void writesAndReadsFromTheFlywayCreatedTable() throws Exception {
		try (var connection = dataSource.getConnection()) {
			connection.setAutoCommit(false);
			try {
				try (var insert = connection.prepareStatement(
						"INSERT INTO persistence_smoke (id, message) VALUES (?, ?)")) {
					insert.setLong(1, 1L);
					insert.setString(2, "Persistence smoke test");
					assertEquals(1, insert.executeUpdate());
				}
				try (var select = connection.prepareStatement(
						"SELECT message FROM persistence_smoke WHERE id = ?")) {
					select.setLong(1, 1L);
					try (var rows = select.executeQuery()) {
						assertTrue(rows.next());
						assertEquals("Persistence smoke test", rows.getString("message"));
						assertFalse(rows.next());
					}
				}
			}
			finally {
				// Keep tests independent even when an assertion fails.
				connection.rollback();
			}
		}
	}

	@Test
	void hibernateValidatesInsteadOfChangingSchema() {
		assertEquals("validate", entityManagerFactory.getProperties().get("hibernate.hbm2ddl.auto"));
	}

	@Test
	void flywayValidationIsEnabledAndCleanIsDisabled() {
		assertTrue(flyway.getConfiguration().isValidateOnMigrate());
		assertTrue(flyway.getConfiguration().isCleanDisabled());
	}

	@Test
	void persistenceContextDoesNotExtendIntoWebRendering() {
		assertTrue(applicationContext.getBeansOfType(OpenEntityManagerInViewInterceptor.class).isEmpty(),
				"Disable Open EntityManager in View to keep persistence work inside application boundaries");
	}
}
