package com.sap.cloud.lm.sl.cf.database.migration;

import java.sql.SQLException;
import java.util.Arrays;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.sap.cloud.lm.sl.cf.database.migration.executor.DatabaseSequenceMigrationExecutor;
import com.sap.cloud.lm.sl.cf.database.migration.executor.DatabaseTableMigrationExecutor;
import com.sap.cloud.lm.sl.cf.database.migration.executor.ImmutableDatabaseSequenceMigrationExecutor;
import com.sap.cloud.lm.sl.cf.database.migration.executor.ImmutableDatabaseTableMigrationExecutor;
import com.sap.cloud.lm.sl.cf.database.migration.extractor.DataSourceEnvironmentExtractor;

public class DatabaseMigration {

    private final static Logger LOGGER = Logger.getLogger(DatabaseMigration.class);

    public static void main(String[] args) throws SQLException {
        configureLogger();
        LOGGER.info("Starting database migration...");
        DataSourceEnvironmentExtractor environmentExtractor = new DataSourceEnvironmentExtractor();
        DataSource sourceDataSource = environmentExtractor.extractDataSource("deploy-service-database-source");
        DataSource targetDataSource = environmentExtractor.extractDataSource("deploy-service-database");

        DatabaseSequenceMigrationExecutor sequenceMigrationExecutor = ImmutableDatabaseSequenceMigrationExecutor.builder()
                                                                                                                .sourceDataSource(sourceDataSource)
                                                                                                                .targetDataSource(targetDataSource)
                                                                                                                .build();

        DatabaseTableMigrationExecutor tableMigrationExecutor = ImmutableDatabaseTableMigrationExecutor.builder()
                                                                                                       .sourceDataSource(sourceDataSource)
                                                                                                       .targetDataSource(targetDataSource)
                                                                                                       .build();
        Arrays.asList("configuration_entry_sequence", "configuration_subscription_sequence")
              .stream()
              .forEach(sequenceMigrationExecutor::executeMigration);

        Arrays.asList("configuration_registry", "configuration_subscription")
              .stream()
              .forEach(tableMigrationExecutor::executeMigration);

        LOGGER.info("Database migration completed.");
    }

    private static void configureLogger() {
        PropertyConfigurator.configure(DatabaseMigration.class.getClassLoader()
                                                              .getResourceAsStream("console-logger.properties"));
    }

}
