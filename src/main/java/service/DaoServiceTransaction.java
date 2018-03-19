package service;

import org.hibernate.Session;

public interface DaoServiceTransaction {
    boolean onTransaction(Session s);
}
