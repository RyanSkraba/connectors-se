package org.talend.components.jdbc.service;

import static java.util.stream.Collectors.toList;

import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import org.talend.components.jdbc.datastore.BasicDatastore;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.completion.DynamicValues;
import org.talend.sdk.component.api.service.completion.Values;
import org.talend.sdk.component.api.service.healthcheck.HealthCheck;
import org.talend.sdk.component.api.service.healthcheck.HealthCheckStatus;

@Service
public class ActionService {

    public static final String ACTION_LIST_SUPPORTED_DB = "ACTION_LIST_SUPPORTED_DB";

    public static final String ACTION_BASIC_HEALTH_CHECK = "ACTION_BASIC_HEALTH_CHECK";

    @Service
    private JdbcService jdbcDriversService;

    @Service
    private I18nMessage i18n;

    @DynamicValues(ACTION_LIST_SUPPORTED_DB)
    public Values loadSupportedDataBaseTypes() {
        return new Values(jdbcDriversService.getDrivers().keySet().stream().map(id -> new Values.Item(id, id)).collect(toList()));
    }

    @HealthCheck(ACTION_BASIC_HEALTH_CHECK)
    public HealthCheckStatus validateBasicDatastore(@Option final BasicDatastore datastore) {
        if (datastore.getJdbcUrl() == null || datastore.getJdbcUrl().isEmpty()) {
            throw new IllegalArgumentException(i18n.errorEmptyJdbcURL());
        }
        final URLClassLoader loader = jdbcDriversService.getDriverClassLoader(datastore.getDbType());
        if (loader == null) {
            return new HealthCheckStatus(HealthCheckStatus.Status.KO, i18n.errorCantLoadDriver(datastore.getDbType()));
        }
        try {
            Driver driver = Driver.class
                    .cast(loader.loadClass(jdbcDriversService.getDrivers().get(datastore.getDbType()).getClazz()).newInstance());

            if (!driver.acceptsURL(datastore.getJdbcUrl())) {
                return new HealthCheckStatus(HealthCheckStatus.Status.KO, i18n.errorUnsupportedSubProtocol());
            }
            final Properties info = new Properties() {

                {
                    setProperty("user", datastore.getUserId());
                    setProperty("password", datastore.getPassword());
                }
            };
            try (Connection connection = driver.connect(datastore.getJdbcUrl(), info)) {
                if (!connection.isValid(15)) {
                    return new HealthCheckStatus(HealthCheckStatus.Status.KO, i18n.errorInvalidConnection());
                }
            }
        } catch (ClassNotFoundException e) {
            return new HealthCheckStatus(HealthCheckStatus.Status.KO, i18n.errorDriverNotFound(datastore.getDbType()));
        } catch (SQLException e) {
            return new HealthCheckStatus(HealthCheckStatus.Status.KO, i18n.errorSQL(e.getErrorCode(), e.getMessage()));
        } catch (IllegalAccessException | InstantiationException e) {
            return new HealthCheckStatus(HealthCheckStatus.Status.KO, i18n.errorDriverInstantiation(e.getMessage()));
        }

        return new HealthCheckStatus(HealthCheckStatus.Status.OK, i18n.successConnection());
    }

}
