package gov.aicte.apaar.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${DATABASE_URL:#{null}}")
    private String databaseUrl;

    @Value("${spring.datasource.url:#{null}}")
    private String springDatasourceUrl;

    @Value("${spring.datasource.username:#{null}}")
    private String springUsername;

    @Value("${spring.datasource.password:#{null}}")
    private String springPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        String dbUrl = databaseUrl != null && !databaseUrl.trim().isEmpty() ? databaseUrl : springDatasourceUrl;

        if (dbUrl != null && (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://") || dbUrl.startsWith("jdbc:postgresql://"))) {
            log.info("Configuring PostgreSQL persistent database connection pool...");
            HikariConfig config = new HikariConfig();

            try {
                if (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://")) {
                    URI dbUri = new URI(dbUrl);
                    String[] userInfo = dbUri.getUserInfo() != null ? dbUri.getUserInfo().split(":") : new String[]{"", ""};
                    String username = userInfo[0];
                    String password = userInfo.length > 1 ? userInfo[1] : "";
                    int port = dbUri.getPort() != -1 ? dbUri.getPort() : 5432;
                    String path = dbUri.getPath();
                    if (path != null && path.startsWith("/")) path = path.substring(1);

                    String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s?sslmode=prefer",
                            dbUri.getHost(), port, path);

                    config.setJdbcUrl(jdbcUrl);
                    config.setUsername(username);
                    config.setPassword(password);
                } else {
                    config.setJdbcUrl(dbUrl);
                    if (springUsername != null) config.setUsername(springUsername);
                    if (springPassword != null) config.setPassword(springPassword);
                }

                config.setDriverClassName("org.postgresql.Driver");
                config.setMaximumPoolSize(10);
                config.setMinimumIdle(2);
                config.setIdleTimeout(30000);
                config.setMaxLifetime(1800000);
                config.setConnectionTimeout(20000);

                log.info("PostgreSQL HikariCP DataSource successfully initialized.");
                return new HikariDataSource(config);
            } catch (Exception e) {
                log.error("Failed to parse PostgreSQL DATABASE_URL. Falling back to embedded H2: {}", e.getMessage());
            }
        }

        // Fallback to fast embedded H2 database for local testing and development
        log.info("No PostgreSQL DATABASE_URL detected. Initializing embedded H2 database (mem:apaardb)...");
        HikariConfig h2Config = new HikariConfig();
        h2Config.setDriverClassName("org.h2.Driver");
        h2Config.setJdbcUrl("jdbc:h2:mem:apaardb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        h2Config.setUsername("sa");
        h2Config.setPassword("");
        return new HikariDataSource(h2Config);
    }
}
