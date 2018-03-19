package service;

import dao.ImageDuplicateProtect;
import dao.ImageId;
import org.apache.commons.codec.binary.Hex;

import org.h2.mvstore.Cursor;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import utils.CryptUtils;
import utils.ImageUtils;
import utils.Loggable;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.stream.Collectors;
;
import static service.RootService.DATASTORAGE_ROOT;

public class DaoService implements Loggable {
    private final ReentrantLock lock = new ReentrantLock();

    private final byte[] masterKey;
    private final byte[] iv;
    private final String storageName;
    private final File storageDir;

    private final SessionFactory currSF;
    private final StandardServiceRegistry registry;
    private final HashMap<String, Session> sessions = new HashMap<>();

    public DaoService(byte[] authData) {
        if (Objects.isNull(authData)) throw new IllegalArgumentException("authData cannot be null");

        final byte[] sha512 = CryptUtils.sha512(authData);
        this.masterKey = Arrays.copyOfRange(sha512, 16, 48);
        this.iv = Arrays.copyOfRange(sha512, 48, 64);
        this.storageName = Hex.encodeHexString(Arrays.copyOfRange(CryptUtils.sha512(sha512), 0, 16), true);
        final String dirPath = DATASTORAGE_ROOT + File.separator + "databases" + File.separator;
        this.storageDir = new File(dirPath + storageName).getAbsoluteFile();
        new File(dirPath).mkdirs();

        final String dbURI = "jdbc:h2:" + this.storageDir + ";CIPHER=AES;";
        try {
            final String dbPassword = Hex.encodeHexString(Arrays.copyOfRange(CryptUtils.sha256(sha512), 0, 21), true);
            final Properties prop = new Properties();
            prop.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
            prop.setProperty("hibernate.hbm2ddl.auto", "update");
            prop.setProperty("hibernate.connection.url", dbURI);
            prop.setProperty("hibernate.connection.username", "default");
            prop.setProperty("hibernate.connection.password", dbPassword + " " + dbPassword);
            prop.setProperty("dialect", "org.hibernate.dialect.H2Dialect");
            prop.setProperty("hibernate.show_sql", "true");
            //prop.setProperty("hibernate.format_sql", "true");

            prop.setProperty("connection.provider_class", "org.hibernate.connection.C3P0ConnectionProvider");
            prop.setProperty("hibernate.c3p0.acquire_increment", "1");
            prop.setProperty("hibernate.c3p0.idle_test_period", "30");
            prop.setProperty("hibernate.c3p0.min_size", "1");
            prop.setProperty("hibernate.c3p0.max_size", "2");
            prop.setProperty("hibernate.c3p0.max_statements", "50");
            prop.setProperty("hibernate.c3p0.timeout", "0");
            prop.setProperty("hibernate.c3p0.acquireRetryAttempts", "1");
            prop.setProperty("hibernate.c3p0.acquireRetryDelay", "250");

            prop.setProperty("hibernate.transaction.factory_class", "org.hibernate.transaction.JDBCTransactionFactory");
            prop.setProperty("hibernate.current_session_context_class", "thread");

            final Configuration conf = new Configuration()
                    .addProperties(prop)
                    .addAnnotatedClass(ImageDuplicateProtect.class)
                    .addAnnotatedClass(ImageId.class)
                    .configure();

            registry = new StandardServiceRegistryBuilder()
                    .applySettings(conf.getProperties())
                    .build();

            currSF = conf.buildSessionFactory(registry);
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public void transaction(DaoServiceTransaction daoServiceTransaction) {
        final Session s = getSession();
        s.beginTransaction();
        try {
            if (daoServiceTransaction.onTransaction(s)) {
                s.getTransaction().commit();
            } else {
                s.getTransaction().rollback();
                s.clear();
                W("Bad transaction: cancelled by user; thread " + Thread.currentThread().getName());
            }
        } catch (ConstraintViolationException e) {
            s.getTransaction().rollback();
            s.clear();
            W("Bad transaction: cancelled by " + e.getClass().getSimpleName() + "; thread " + Thread.currentThread().getName() + "; message: " + e.getMessage());
        }
    }

    private Session getSession() {
        final String threadName = Thread.currentThread().getName();
        if (!sessions.containsKey(threadName)) {
            final Session s = currSF.openSession();
            sessions.put(threadName, s);
        }

       final Session s = sessions.get(threadName);
         if (!s.isOpen()) {
            final Session s1 = currSF.openSession();
            sessions.remove(threadName);
            sessions.put(threadName, s1);
        }

        return s;
    }

    /***********************************************************************************************************************/

    public synchronized boolean hasDuplicates(Path p) {
        final ImageDuplicateProtect ip1 = new ImageDuplicateProtect(p);
        List list = getSession()
                .createCriteria(ImageDuplicateProtect.class)
                .add(Restrictions.eq("hashOfNameAndSize", ip1.getHashOfNameAndSize()))
                .list();
        return Objects.nonNull(list) && !list.isEmpty();
    }

    public synchronized void pushImageId(Path p, String imgId) {
        final ImageId imageId = new ImageId(imgId);
        final ImageDuplicateProtect imageDuplicateProtect = new ImageDuplicateProtect(p);
        transaction(s -> {
            s.save(imageId);
            s.save(imageDuplicateProtect);
            return true;
        });
        return;
    }

    public synchronized List<ImageId> getImageIdList(long[] in) {
        final Long[] array = new Long[in.length];
        for (int i=0; i<in.length; i++) array[i] = in[i];
        return getSession()
                .createCriteria(ImageId.class)
                .add(Restrictions.in("oid", array))
                .addOrder(Order.desc("oid"))
                .list();
    }

    /* I don't use LIMIT(A,B), it's too slow; This hack was increase program speed up to 10x-30x, but required some memory; */
    public long[] generateCache() {
        final List list = getSession()
                .createCriteria(ImageId.class)
                .setProjection(Projections.property("oid"))
                .addOrder(Order.desc("oid"))
                .list();
        if (Objects.nonNull(list) && !list.isEmpty() && (list.get(0) instanceof Long)) {
            final long[] array = new long[list.size()];
            for (int i=0; i<list.size(); i++) array[i] = ((Long) list.get(i));
            L("generateCache: " + array.length + " images ids loaded");
            return array;
        } else {
            E("generateCache: bad data");
        }
        return null;
    }




    /*


    public synchronized long getImageIdTotalCount() {
        return imageIdRepository.size();
    }*/







    public void dispose() {
        sessions.values().forEach(session -> session.close());
        currSF.close();
        StandardServiceRegistryBuilder.destroy(registry);
    }
}
