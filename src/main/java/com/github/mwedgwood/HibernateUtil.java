package com.github.mwedgwood;

import com.github.mwedgwood.model.Model;
import org.h2.jdbcx.JdbcDataSource;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.ServiceRegistry;
import org.reflections.Reflections;

import javax.persistence.Entity;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.hibernate.cfg.AvailableSettings.*;

public class HibernateUtil {

    private SessionFactory sessionFactory;

    private static class SingletonHolder {
        private static final HibernateUtil INSTANCE = new HibernateUtil();
    }

    private HibernateUtil() {
        initializeSessonFactory();
    }

    public static SessionFactory getSessionFactory() {
        return SingletonHolder.INSTANCE.sessionFactory;
    }

    private void initializeSessonFactory() {
        Configuration configuration = new Configuration();
        configuration.setProperty(Environment.CONNECTION_PROVIDER, H2ConnectionProvider.class.getName());
        configuration.setProperty(DIALECT, H2Dialect.class.getName());
        configuration.setProperty(HBM2DDL_AUTO, "create-drop");
        configuration.setProperty(USE_SECOND_LEVEL_CACHE, Boolean.FALSE.toString());
        configuration.setProperty(USE_QUERY_CACHE, Boolean.FALSE.toString());
        configuration.setProperty(SHOW_SQL, Boolean.TRUE.toString());
        configuration.setProperty(FORMAT_SQL, Boolean.TRUE.toString());
        addAnnotatedClasses(configuration);

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .build();

        sessionFactory = configuration.buildSessionFactory(serviceRegistry);
    }

    private Configuration addAnnotatedClasses(Configuration configuration) {
        for (Class<?> aClass : new Reflections(Model.class.getPackage().getName()).getTypesAnnotatedWith(Entity.class)) {
            // don't load classes that have generic type parameters as hibernate can not deal with them unless used as superclasses
            if (aClass.getTypeParameters().length == 0) configuration.addAnnotatedClass(aClass);
        }
        return configuration;
    }

    private class H2ConnectionProvider implements ConnectionProvider {

        DataSource getDataSource() {
            JdbcDataSource jdbcDataSource = new JdbcDataSource();
            jdbcDataSource.setURL("jdbc:h2:mem");
            return jdbcDataSource;
        }

        @Override
        public Connection getConnection() throws SQLException {
            return getDataSource().getConnection();
        }

        @Override
        public void closeConnection(Connection conn) throws SQLException {
            conn.close();
        }

        @Override
        public boolean supportsAggressiveRelease() {
            return false;
        }

        @Override
        public boolean isUnwrappableAs(Class unwrapType) {
            return false;
        }

        @Override
        public <T> T unwrap(Class<T> unwrapType) {
            return null;
        }
    }

}
