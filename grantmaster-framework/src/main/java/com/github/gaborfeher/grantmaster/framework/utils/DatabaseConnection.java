package com.github.gaborfeher.grantmaster.framework.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates everything that is needed for having an open database
 * connection and provides an EntityManagerFactory for accessing the
 * database.
 */
public class DatabaseConnection {
  private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);

  /**
   * The unpacked database files used by the database engine.
   */
  private DatabaseArchive archive;
  /**
   * true if there was a successful transaction since the last save.
   */
  private boolean unsavedChanges;
  private EntityManagerFactory entityManagerFactory;
  
  public void close() {
    disconnect();
    if (archive != null) {
      archive.close();
      archive = null;
    }
  }
    
  private void disconnect() {
    unsavedChanges = false;
    if (getEntityManagerFactory() != null) {
      getEntityManagerFactory().close();
      entityManagerFactory = null;
    }
  }
  
  private boolean connectToJdbcUrl(String jdbcUrl) {
    if (getEntityManagerFactory() != null) {
      throw new RuntimeException("Cannot connect while previous connection is active.");
    }
    entityManagerFactory = null;
    Map<String, String> properties = new HashMap<>();
    properties.put("javax.persistence.jdbc.url", jdbcUrl);
    try {
      entityManagerFactory = Persistence.createEntityManagerFactory(
          "LocalH2ConnectionTemplate", properties);
    } catch (PersistenceException ex) {
      LoggerFactory.getLogger(DatabaseSingleton.class).error(null, ex);
    }
    return getEntityManagerFactory() != null;

  }
  
  private boolean connectToMemoryFileForTesting() {
    return connectToJdbcUrl("jdbc:hsqldb:mem:test;shutdown=true");
  }
  
  private boolean connectToFile() {
    return connectToJdbcUrl("jdbc:hsqldb:file:" + new File(archive.getFile(), "database") +";shutdown=true");
  }
  
  public void saveDatabase(File path) throws IOException {
    logger.info("saveDatabase({})", path);
    disconnect();
    archive.saveTo(path);
    unsavedChanges = false;
    connectToFile();
  }

  public static DatabaseConnection createNewDatabase() {
    logger.info("createNewDatabase");
    DatabaseConnection newConnection = new DatabaseConnection();
    newConnection.archive = DatabaseArchive.createNew();
    if (newConnection.archive == null) {
      return null;
    }
    if (!newConnection.connectToFile()) {
      newConnection.close();
      return null;
    }
    return newConnection;
  }
  
  static DatabaseConnection createNewMemoryDatabaseForTesting() {
    DatabaseConnection newConnection = new DatabaseConnection();
    newConnection.archive = DatabaseArchive.createNew();
    if (newConnection.archive == null ||
        !newConnection.connectToMemoryFileForTesting()) {
      return null;
    }
    return newConnection;
  }
  
  
  public static DatabaseConnection openDatabase(File path) {
    logger.info("openDatabase({})", path);
    DatabaseConnection newConnection = new DatabaseConnection();
    newConnection.archive = DatabaseArchive.open(path);
    if (newConnection.archive == null) {
      return null;
    }
    if (!newConnection.connectToFile()) {
      newConnection.close();
      return null;
    }
    return newConnection;
  }
  
  EntityManagerFactory getEntityManagerFactory() {
    return entityManagerFactory;
  }

  public boolean isUnsavedChanges() {
    return unsavedChanges;
  }

  void setUnsavedChanges(boolean unsavedChanges) {
    this.unsavedChanges = unsavedChanges;
  }

}
