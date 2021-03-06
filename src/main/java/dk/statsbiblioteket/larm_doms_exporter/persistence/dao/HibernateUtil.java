package dk.statsbiblioteket.larm_doms_exporter.persistence.dao;

import dk.statsbiblioteket.larm_doms_exporter.persistence.DomsExportRecord;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

import java.io.File;

/**
 * Hibernate utility for building hibernate sessions for this set of applications from a configuration file.
 */
public class HibernateUtil implements HibernateUtilIF {

    private static SessionFactory factory;

    private static HibernateUtil instance;
    private static String configFilePath;

    private HibernateUtil() {

    }

    /**
     * Encapsulate logic for hibernate sessions for persisting instances of the DomsExportRecord class.
     * @param configFilePath  the Configuration file.
     * @return  the singleton instance of this class
     */
    public static synchronized HibernateUtil getInstance(String configFilePath) {
        HibernateUtil.configFilePath = configFilePath;
        if (instance == null) {
            instance = new HibernateUtil();
            File file = new File(configFilePath);
            AnnotationConfiguration configure = (new AnnotationConfiguration()).configure(file);
            configure.addAnnotatedClass(DomsExportRecord.class);
            factory = configure.buildSessionFactory();
        }
        return instance;
    }

    @Override
    public SessionFactory getSessionFactory() {
        return factory;
    }

    @Override
    public Session getSession() {
        return factory.openSession();
    }

    public void reload(){
        instance = null;
        instance = getInstance(configFilePath);
    }
}
