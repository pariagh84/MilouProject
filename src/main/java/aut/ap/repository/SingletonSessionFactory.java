package aut.ap.repository;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class SingletonSessionFactory {
    private static SessionFactory sessionFactory = null;

    public static SessionFactory get() {
        if (sessionFactory == null) {
            return sessionFactory = new Configuration()
                    .configure("hibernate.cfg.xml")
                    .buildSessionFactory();
        }
        return sessionFactory;
    }

    public static void close() {
        sessionFactory.close();
    }
}
