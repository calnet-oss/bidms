package edu.berkeley.bidms.orm.transaction;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;
import org.springframework.lang.NonNull;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.util.Objects;

public class JpaTransactionTemplate extends TransactionTemplate {
    public JpaTransactionTemplate(@NonNull PlatformTransactionManager transactionManager) {
        super(transactionManager);
    }

    public JpaTransactionTemplate(
            @NonNull PlatformTransactionManager transactionManager,
            @NonNull TransactionDefinition transactionDefinition
    ) {
        super(transactionManager, transactionDefinition);
    }

    public JpaTransactionTemplate(
            @NonNull PlatformTransactionManager transactionManager,
            int propagationBehavior
    ) {
        super(transactionManager, new DefaultTransactionDefinition(propagationBehavior));
    }

    public JpaTransactionTemplate(
            @NonNull PlatformTransactionManager transactionManager,
            int propagationBehavior,
            @NonNull String transactionName
    ) {
        super(transactionManager, new NameableDefaultTransactionDefinition(propagationBehavior, transactionName));
    }

    private static class NameableDefaultTransactionDefinition extends DefaultTransactionDefinition {
        NameableDefaultTransactionDefinition(@NonNull String name) {
            super();
            setName(name);
        }

        NameableDefaultTransactionDefinition(int propagationBehavior, @NonNull String name) {
            super(propagationBehavior);
            setName(name);
        }
    }

    @NonNull
    public EntityManager getCurrentEntityManager() {
        JpaTransactionManager jpaTransactionManager = Objects.requireNonNull((JpaTransactionManager) getTransactionManager());
        EntityManagerHolder holder = Objects.requireNonNull((EntityManagerHolder) TransactionSynchronizationManager.getResource(
                Objects.requireNonNull(jpaTransactionManager.getEntityManagerFactory())
        ));
        return holder.getEntityManager();
    }

    @NonNull
    public EntityTransaction getCurrentEntityTransaction() {
        return Objects.requireNonNull(getCurrentEntityManager().getTransaction());
    }

    public String getCurrentTransactionName() {
        return TransactionSynchronizationManager.getCurrentTransactionName();
    }

    /*public static <T> T withNewTransaction(PlatformTransactionManager transactionManager, TransactionCallback<T> callback) {
        return new JpaTransactionTemplate(transactionManager).execute(callback);
    }

    public static <T> T withNewTransaction(PlatformTransactionManager transactionManager, int propagationBehavior, TransactionCallback<T> callback) {
        return new JpaTransactionTemplate(transactionManager, propagationBehavior).execute(callback);
    }

    public static void withNewTransactionWithoutResult(PlatformTransactionManager transactionManager, TransactionCallbackWithoutResult callback) {
        new JpaTransactionTemplate(transactionManager).execute(callback);
    }

    public static void withNewTransactionWithoutResult(PlatformTransactionManager transactionManager, int propagationBehavior, TransactionCallbackWithoutResult callback) {
        new JpaTransactionTemplate(transactionManager, propagationBehavior).execute(callback);
    }*/

    public static Connection getConnection(TransactionStatus transactionStatus) {
        if (transactionStatus instanceof DefaultTransactionStatus) {
            Object rawTransaction = ((DefaultTransactionStatus) transactionStatus).getTransaction();
            if (rawTransaction instanceof JdbcTransactionObjectSupport) {
                return ((JdbcTransactionObjectSupport) rawTransaction).getConnectionHolder().getConnection();
            } else {
                throw new UnsupportedOperationException("Only transaction objects that are instances of JdbcTransactionObjectSupport are supported");
            }
        } else {
            throw new UnsupportedOperationException("Only transactionStatus type of DefaultTransactionStatus supported");
        }
    }
}
