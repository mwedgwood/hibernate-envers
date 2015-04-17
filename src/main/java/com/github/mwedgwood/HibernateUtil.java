package com.github.mwedgwood;

import com.github.mwedgwood.model.Person;
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

import static java.lang.Boolean.TRUE;
import static org.hibernate.cfg.AvailableSettings.*;
import static org.hibernate.envers.event.spi.EnversIntegrator.AUTO_REGISTER;

public class HibernateUtil {

    private SessionFactory sessionFactory;

    private static class SingletonHolder {
        private static final HibernateUtil INSTANCE = new HibernateUtil();
    }

    private HibernateUtil() {
        initializeSessionFactory();
    }

    public static SessionFactory getSessionFactory() {
        return SingletonHolder.INSTANCE.sessionFactory;
    }

    private void initializeSessionFactory() {
        Configuration configuration = new Configuration()
                .setProperty(Environment.CONNECTION_PROVIDER, H2ConnectionProvider.class.getName())
                .setProperty(CURRENT_SESSION_CONTEXT_CLASS, "thread")
                .setProperty(DIALECT, H2Dialect.class.getName())
                .setProperty(HBM2DDL_AUTO, "create-drop")
                .setProperty(SHOW_SQL, Boolean.TRUE.toString())
                .setProperty(FORMAT_SQL, Boolean.TRUE.toString())
                .setProperty(AUTO_REGISTER, TRUE.toString());

        addAnnotatedClasses(configuration);

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .build();

        sessionFactory = configuration.buildSessionFactory(serviceRegistry);
    }

    private Configuration addAnnotatedClasses(Configuration configuration) {
        // don't load classes that have generic type parameters as hibernate can not deal with them unless used as superclasses
        new Reflections(Person.class.getPackage().getName()).getTypesAnnotatedWith(Entity.class)
                .stream()
                .filter(aClass -> aClass.getTypeParameters().length == 0)
                .forEach(configuration::addAnnotatedClass);

        return configuration;
    }

    public static class H2ConnectionProvider implements ConnectionProvider {

        DataSource getDataSource() {
            JdbcDataSource jdbcDataSource = new JdbcDataSource();
            jdbcDataSource.setURL("jdbc:h2:~/hibernate_examples");
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
