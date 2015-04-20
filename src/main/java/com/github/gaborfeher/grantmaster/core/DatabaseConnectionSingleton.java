package com.github.gaborfeher.grantmaster.core;

import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectExpenseWrapper;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

public class DatabaseConnectionSingleton {
  private static DatabaseConnectionSingleton instance;
  
  private EntityManagerFactory entityManagerFactory;
  private EntityManager entityManager;
  
  private DatabaseConnectionSingleton() {
    
  }
  
  public static synchronized DatabaseConnectionSingleton getInstance() {
    if (instance == null) {
      instance = new DatabaseConnectionSingleton();
    }
    return instance;
  }
  
  public void close() {
    if (entityManager != null) {
      entityManager.close();
      entityManager = null;
    }
    if (entityManagerFactory != null) {
      entityManagerFactory.close();
      entityManagerFactory = null;
    }
  }
  
  public boolean connectTo(String pathString) {
    Map<String, String> properties = new HashMap<>();
    properties.put("javax.persistence.jdbc.url", "jdbc:h2:" + pathString);
    try {
      entityManagerFactory = Persistence.createEntityManagerFactory(
          "LocalH2ConnectionTemplate", properties);
      entityManager = entityManagerFactory.createEntityManager();
    } catch (PersistenceException ex) {
      return false;
    }
    System.out.println("Successful JPA connection");
    return true;
  }
  
  public void persist(final Object obj) {
    runInTransaction(new TransactionRunner() {
      @Override
      public boolean run(EntityManager em) {
        em.persist(obj);
        return true;
      }
    });
  }

  public void remove(final Object obj) {
    runInTransaction(new TransactionRunner() {
      @Override
      public boolean run(EntityManager em) {
        em.remove(obj);
        return true;
      }
      @Override
      public void onFailure() {
        hardReset();
      }
    });
  }
  
  public void hardReset() {
    // TODO: try to figure out when this is exactly needed
    entityManager.close();
    entityManager = entityManagerFactory.createEntityManager();
    RefreshControlSingleton.getInstance().broadcastRefresh();
  }
  
  public boolean runInTransaction(TransactionRunner runner) {
    EntityTransaction transaction = entityManager.getTransaction();
    try {
      transaction.begin();
      if (runner.run(entityManager)) {
        transaction.commit();
      } else {
        transaction.rollback();
        runner.onFailure();
        return false;
      }
    } catch (Throwable t) {
      Logger.getLogger(DatabaseConnectionSingleton.class.getName()).log(Level.SEVERE, null, t);
      if (transaction.isActive()) {
        transaction.rollback();
      }
      runner.onFailure();
      return false;
    }
    runner.onSuccess();
    return true;
  }

  public boolean isConnected() {
    return entityManager != null;
  }

  public <T extends Object> TypedQuery<T> createQuery(String query, Class<T> resultClass) {
    return entityManager.createQuery(query, resultClass);
  }

  public void refresh(Object entity) {
    entityManager.refresh(entity);
  }
}
