package com.devdoyen.nemologic;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("local")
public class LocalProfileConfigurationTest {

    @Autowired
    private Environment environment;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public DataSource dataSource() throws SQLException {
            DataSource mockDataSource = mock(DataSource.class);
            Connection mockConnection = mock(Connection.class);
            DatabaseMetaData mockMetaData = mock(DatabaseMetaData.class);

            when(mockDataSource.getConnection()).thenReturn(mockConnection);
            when(mockConnection.getMetaData()).thenReturn(mockMetaData);

            // Connection schema & catalog
            when(mockConnection.getSchema()).thenReturn("public");
            when(mockConnection.getCatalog()).thenReturn("nemologic");
            when(mockConnection.getAutoCommit()).thenReturn(true);
            when(mockConnection.isClosed()).thenReturn(false);

            // DatabaseMetaData
            when(mockMetaData.getDatabaseProductName()).thenReturn("PostgreSQL");
            when(mockMetaData.getDatabaseProductVersion()).thenReturn("14.0");
            when(mockMetaData.getDriverName()).thenReturn("PostgreSQL JDBC Driver");
            when(mockMetaData.getDriverVersion()).thenReturn("42.7.3");
            when(mockMetaData.getDefaultTransactionIsolation()).thenReturn(Connection.TRANSACTION_READ_COMMITTED);
            when(mockMetaData.getDatabaseMajorVersion()).thenReturn(14);
            when(mockMetaData.getDatabaseMinorVersion()).thenReturn(0);
            when(mockMetaData.getJDBCMajorVersion()).thenReturn(4);
            when(mockMetaData.getJDBCMinorVersion()).thenReturn(2);
            when(mockMetaData.getIdentifierQuoteString()).thenReturn("\"");
            when(mockMetaData.supportsMixedCaseIdentifiers()).thenReturn(false);
            when(mockMetaData.storesUpperCaseIdentifiers()).thenReturn(false);
            when(mockMetaData.storesLowerCaseIdentifiers()).thenReturn(false);
            when(mockMetaData.storesMixedCaseIdentifiers()).thenReturn(true);
            when(mockMetaData.getSQLKeywords()).thenReturn("");
            when(mockMetaData.getConnection()).thenReturn(mockConnection);

            // PreparedStatement mocking
            java.sql.PreparedStatement mockStatement = mock(java.sql.PreparedStatement.class);
            java.sql.ResultSet sequenceResultSet = mock(java.sql.ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(sequenceResultSet);
            when(sequenceResultSet.next()).thenReturn(false);

            // Statement mocking for DDL generation
            java.sql.Statement mockSchemaStatement = mock(java.sql.Statement.class);
            when(mockConnection.createStatement()).thenReturn(mockSchemaStatement);

            // Mocking ResultSet for schemas/catalogs lookup
            java.sql.ResultSet emptyResultSet = mock(java.sql.ResultSet.class);
            when(emptyResultSet.next()).thenReturn(false);
            when(mockMetaData.getSchemas()).thenReturn(emptyResultSet);
            when(mockMetaData.getSchemas(nullable(String.class), nullable(String.class))).thenReturn(emptyResultSet);
            when(mockMetaData.getCatalogs()).thenReturn(emptyResultSet);
            when(mockMetaData.getTables(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String[].class))).thenReturn(emptyResultSet);
            when(mockMetaData.getColumns(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class))).thenReturn(emptyResultSet);
            when(mockMetaData.getPrimaryKeys(nullable(String.class), nullable(String.class), nullable(String.class))).thenReturn(emptyResultSet);
            when(mockMetaData.getImportedKeys(nullable(String.class), nullable(String.class), nullable(String.class))).thenReturn(emptyResultSet);
            when(mockMetaData.getIndexInfo(nullable(String.class), nullable(String.class), nullable(String.class), anyBoolean(), anyBoolean())).thenReturn(emptyResultSet);

            return mockDataSource;
        }
    }

    @Test
    void contextLoadsInLocalProfile() {
        String activeProfile = environment.getActiveProfiles()[0];
        assertEquals("local", activeProfile);

        String driver = environment.getProperty("spring.datasource.driver-class-name");
        assertEquals("org.postgresql.Driver", driver);

        String maxPool = environment.getProperty("spring.datasource.hikari.maximum-pool-size");
        assertEquals("10", maxPool);
    }
}
