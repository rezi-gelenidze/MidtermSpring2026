package persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class PersistenceUtil {

    private static EntityManagerFactory emf;

    public static void init(String persistenceUnit) {
        if (emf == null || !emf.isOpen()) {
            emf = Persistence.createEntityManagerFactory(persistenceUnit);
        }
    }

    public static EntityManager createEntityManager() {
        if (emf == null) {
            init("uno-prod");
        }
        return emf.createEntityManager();
    }

    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            emf = null;
        }
    }
}
