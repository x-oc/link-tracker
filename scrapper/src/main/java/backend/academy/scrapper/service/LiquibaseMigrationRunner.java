package backend.academy.scrapper.service;

import backend.academy.scrapper.config.ScrapperConfig;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import liquibase.Scope;
import liquibase.command.CommandScope;
import liquibase.command.core.UpdateCommandStep;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.DirectoryResourceAccessor;
import liquibase.resource.ResourceAccessor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("!test")
@Component
@RequiredArgsConstructor
public class LiquibaseMigrationRunner implements CommandLineRunner {

    private final DataSource dataSource;
    private final ScrapperConfig config;

    @Override
    public void run(String... args) throws Exception {
        migrate(Path.of(config.migrationsPath()));
    }

    public void migrate(Path changelogPath) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            Database database =
                    DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn));

            ResourceAccessor resourceAccessor = new DirectoryResourceAccessor(changelogPath);

            Map<String, Object> scopeValues = new HashMap<>();
            scopeValues.put(Scope.Attr.resourceAccessor.name(), resourceAccessor);

            Scope.child(scopeValues, () -> {
                CommandScope updateCommand = new CommandScope("update");
                updateCommand.addArgumentValue("database", database);
                updateCommand.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "master.xml");
                updateCommand.execute();
                return null;
            });
        }
    }
}
