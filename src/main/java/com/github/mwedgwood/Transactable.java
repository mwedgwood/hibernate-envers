package com.github.mwedgwood;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class Transactable {

    public static <T> T execute(UnitOfWork<T> work) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        if (session.getTransaction().isActive()) {
            return work.doWork(session);
        } else {
            Transaction transaction = session.beginTransaction();
            try {
                T result = work.doWork(session);
                transaction.commit();
                return result;
            } finally {
                if (!transaction.wasCommitted()) {
                    transaction.rollback();
                }
            }
        }
    }

    @FunctionalInterface
    public interface UnitOfWork<T> {
        T doWork(final Session session);
    }

}
