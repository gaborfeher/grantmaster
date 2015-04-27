package com.github.gaborfeher.grantmaster.core;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

public enum DatabaseSingleton {
  INSTANCE;
  
  private EntityManagerFactory entityManagerFactory;

  /**
   * Handles the archive file which stores the database. null if no
   * database is open.
   */
  private DatabaseArchive archive;

  /**
   * true if there was a successful transaction since the last save.
   */
  private boolean unsavedChanges;
  
  private DatabaseSingleton() {
  }
  
  public void cleanup() {
    close();
    if (archive != null) {
      archive.close();
      archive = null;
    }
  }
    
  private void close() {
    unsavedChanges = false;
    if (entityManagerFactory != null) {
      entityManagerFactory.close();
      entityManagerFactory = null;
    }
  }

  public File createNewDatabase() {
    cleanup();
    archive = DatabaseArchive.createNew();
    if (archive != null && connectToFile(archive.getFile())) {
      return archive.getFile();
    }
    return null;
  }
  
  private boolean connectToJdbcUrl(String jdbcUrl) {
    if (entityManagerFactory != null) {
      throw new RuntimeException("Cannot connect while previous connection is active.");
    }
    entityManagerFactory = null;
    Map<String, String> properties = new HashMap<>();
    properties.put("javax.persistence.jdbc.url", jdbcUrl);
    try {
      entityManagerFactory = Persistence.createEntityManagerFactory(
          "LocalH2ConnectionTemplate", properties);
    } catch (PersistenceException ex) {
      Logger.getLogger(DatabaseSingleton.class.getName()).log(Level.SEVERE, null, ex);
    }
    return entityManagerFactory != null;

  }
  
  public boolean connectToMemoryFileForTesting() {
    return connectToJdbcUrl("jdbc:hsqldb:mem:test;shutdown=true");
  }
  
  private boolean connectToFile(File file) {
    return connectToJdbcUrl("jdbc:hsqldb:file:" + new File(file, "database") +";shutdown=true");
  }
  
  public File saveDatabase(File path) throws IOException {
    close();
    archive.saveTo(path);
    unsavedChanges = false;
    connectToFile(archive.getFile());
    return archive.getFile();
  }
  
  /**
   * Returns the temporary directory where the database files are extracted.
   * @param path
   * @return 
   */
  public File openDatabase(File path) {
    cleanup();
    archive = DatabaseArchive.open(path);
    if (archive != null) {
      connectToFile(archive.getFile());
      return archive.getFile();
    }
    return null;
  }
  
  public boolean transaction(TransactionRunner runner) {
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
      Logger.getLogger(DatabaseSingleton.class.getName()).log(Level.SEVERE, null, t);
      if (transaction.isActive()) {
        transaction.rollback();
      }
      return false;
    } finally {
      entityManager.close();
    }
    unsavedChanges = true;
    return true;
  }
  
  public boolean query(TransactionRunner runner) {
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

  public boolean isConnected() {
    return entityManagerFactory != null;
  }
  
  public boolean getUnsavedChange() {
    return unsavedChanges;
  }

  public void refresh(Object entity) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    entityManager.refresh(entity);
    entityManager.close();
  }
}
