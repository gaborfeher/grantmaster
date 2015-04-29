package com.github.gaborfeher.grantmaster.core;

import java.io.File;
import java.io.IOException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum DatabaseSingleton {
  INSTANCE;

  /**
   * Handles the archive file which stores the database. null if no
   * database is open.
   */
  private DatabaseConnection connection;
  
  private DatabaseSingleton() {
  }
  
  public void setConnection(DatabaseConnection connection) {
    this.connection = connection;
  }
  
  public boolean transaction(TransactionRunner runner) {
    EntityManagerFactory entityManagerFactory = getEntityManagerFactory();
    if (entityManagerFactory == null) {
      return false;
    }
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();
    try {
      transaction.begin();
      if (runner.run(entityManager)) {
        transaction.commit();
      } else {
        transaction.rollback();
        return false;
      }
    } catch (Throwable t) {
      LoggerFactory.getLogger(DatabaseSingleton.class).error(null, t);
      if (transaction.isActive()) {
        transaction.rollback();
      }
      return false;
    } finally {
      entityManager.close();
    }
    connection.setUnsavedChanges(true);
    return true;
  }
  
  public boolean query(TransactionRunner runner) {
    EntityManagerFactory entityManagerFactory = getEntityManagerFactory();
    if (entityManagerFactory == null) {
      return false;
    }
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    boolean result = false;
    try  {
      result = runner.run(entityManager);
    } finally {
      entityManager.close();
    }
    return result;
  }

  private EntityManagerFactory getEntityManagerFactory() {
    if (connection == null) {
      return null;
    }
    return connection.getEntityManagerFactory();
  }
  
  public boolean isConnected() {
    return connection != null;
  }
  
  public boolean getUnsavedChange() {
    return connection != null && connection.isUnsavedChanges();
  }

  public void refresh(Object entity) {
    EntityManager entityManager = getEntityManagerFactory().createEntityManager();
    entityManager.refresh(entity);
    entityManager.close();
  }

  public void cleanup() {
    if (connection != null) {
      connection.cleanup();
    }
  }

  public void saveDatabase(File path) throws IOException {
    if (connection != null) {
      connection.saveDatabase(path);
    }
  }

  public boolean connectToMemoryFileForTesting() {
    connection = DatabaseConnection.createNewMemoryDatabaseForTesting();
    return connection != null;
  }
}
