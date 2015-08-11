/*
 * This file is a part of GrantMaster.
 * Copyright (C) 2015  Gábor Fehér <feherga@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.gaborfeher.grantmaster.framework.utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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

  private final String PROPERTIES_FILE = "grantmaster.properties";

  /**
   * The archive file from which the database was unpacked. null for newly
   * created and unsaved databases.
   */
  private File originalArchiveFile;

  /**
   * Lock on the opened archive file (originalArchiveFile).
   */
  private MyFileLock lock;

  /**
   * The unpacked database files used by the database engine.
   */
  private DatabaseArchive archive;
  /**
   * true if there was a successful transaction since the last save.
   */
  private boolean unsavedChanges;
  private EntityManagerFactory entityManagerFactory;
  private Properties properties;

  public void close() {
    disconnect();
    if (archive != null) {
      archive.close();
      archive = null;
    }
    originalArchiveFile = null;
    if (lock != null) {
      lock.release();
      lock = null;
    }
  }

  private void disconnect() {
    unsavedChanges = false;
    if (getEntityManagerFactory() != null) {
      getEntityManagerFactory().close();
      entityManagerFactory = null;
    }
  }

  final static String FORMAT_VERSION = "format.version";
  final static int CURRENT_FORMAT_VERSION = 3;

  private File getPropertiesFile() {
    return new File(archive.getDirectory(), PROPERTIES_FILE);
  }

  private void loadOrCreateProperties() {
    File propertiesFile = getPropertiesFile();
    properties = new Properties();
    try {
      if (propertiesFile.exists()) {
        properties.load(new FileReader(propertiesFile));
      } else {
        // In case of empty file,
        properties.put(FORMAT_VERSION, String.format("%d", 0));
      }
    } catch (IOException ex) {
      logger.error(null, ex);
    }
  }

  private void storeProperties() {
    try {
      File propertiesFile = new File(archive.getDirectory(), PROPERTIES_FILE);
      properties.store(new FileWriter(propertiesFile), "This is a GrantMaster database archive.\nhttps://github.com/gaborfeher/grantmaster");
    } catch (IOException ex) {
      LoggerFactory.getLogger(DatabaseSingleton.class).error(
          "Cannot write properties file", ex);
    }
  }

  private int getVersion() {
    if (!properties.containsKey(FORMAT_VERSION)) {
      return 0;
    }
    return Integer.parseInt((String) properties.get(FORMAT_VERSION));
  }

  private boolean checkVersionAndConvert(int currentFormatVersion, String jdbcUrl) {
    switch (currentFormatVersion) {
      case CURRENT_FORMAT_VERSION:
        return true;
      // Earlier versions can be handled here.
      case 0:
      case 1:
        return upgradeVersionFrom01to3(jdbcUrl);
      case 2:
        return false;  // Not supported development version.
      default:
        return false;
    }
  }

  private boolean upgradeVersionFrom01to3(String jdbcUrl) {
    LoggerFactory.getLogger(DatabaseSingleton.class).info("upgradeVersionFrom01to3()");
    Properties connectionProps = new Properties();
    try (
        Connection conn = DriverManager.getConnection(jdbcUrl, connectionProps);
        Statement statement = conn.createStatement()) {
      statement.execute("ALTER TABLE PUBLIC.ProjectReport ADD COLUMN status INTEGER DEFAULT 0 NOT NULL;");
      statement.execute("ALTER TABLE ProjectSource ADD COLUMN comment VARCHAR(255) DEFAULT NULL;");
      statement.execute("ALTER TABLE ProjectExpense ADD COLUMN exchangeRateOverride NUMERIC(25, 10) DEFAULT NULL;");
      statement.execute("ALTER TABLE ProjectExpense ADD COLUMN accountingCurrencyAmountOverride NUMERIC(25, 10) DEFAULT NULL;");
      properties.setProperty(FORMAT_VERSION, "3");
      return true;
    } catch (SQLException ex) {
      LoggerFactory.getLogger(DatabaseSingleton.class).error(null, ex);
      return false;
    }
  }

  private boolean connectToJdbcUrl(String jdbcUrl) {
    if (getEntityManagerFactory() != null) {
      throw new RuntimeException("Cannot connect while previous connection is active.");
    }
    entityManagerFactory = null;
    Map<String, String> connectionProperties = new HashMap<>();
    connectionProperties.put("javax.persistence.jdbc.url", jdbcUrl);
    try {
      entityManagerFactory = Persistence.createEntityManagerFactory(
          "LocalH2ConnectionTemplate", connectionProperties);
    } catch (PersistenceException ex) {
      LoggerFactory.getLogger(DatabaseSingleton.class).error(null, ex);
    }
    return getEntityManagerFactory() != null;
  }

  private boolean connectToMemoryFileForTesting() {
    return connectToJdbcUrl("jdbc:hsqldb:mem:test;shutdown=true");
  }

  /**
   * Given an unpacked database archive, build up a database connection
   * to the database files.
   * @return true on success.
   */
  private boolean connectToFile() {
    File databaseFile = new File(archive.getDirectory(), "database");
    String jdbcUrl = "jdbc:hsqldb:file:" + databaseFile +";shutdown=true";
    if (!checkVersionAndConvert(getVersion(), jdbcUrl)) {
      return false;
    }
    if (!connectToJdbcUrl(jdbcUrl)) {
      return false;
    }
    return true;
  }

  private boolean saveDatabaseInternal(File path) {
    try {
      disconnect();
      storeProperties();
      archive.saveToArchiveFile(path);
      unsavedChanges = false;
      connectToFile();
      return true;
    } catch (IOException ex) {
      logger.error(null, ex);
      return false;
    }
  }

  public boolean saveDatabase() {
    logger.info("saveDatabase()");
    return saveDatabaseInternal(originalArchiveFile);
  }

  public boolean saveAsDatabase(File path) {
    MyFileLock newLock = MyFileLock.lockFile(path);
    if (newLock == null) {
      return false;
    }
    logger.info("saveAsDatabase({})", path);
    if (saveDatabaseInternal(path)) {
      originalArchiveFile = path;
      if (lock != null) {
        lock.release();
      }
      lock = newLock;
      return true;
    } else {
      newLock.release();
      return false;
    }
  }

  public static DatabaseConnection createNewDatabase() {
    logger.info("createNewDatabase");
    DatabaseConnection newConnection = new DatabaseConnection();
    newConnection.archive = DatabaseArchive.createNew();
    newConnection.originalArchiveFile = null;
    newConnection.lock = null;
    newConnection.properties = new Properties();
    newConnection.properties.put(FORMAT_VERSION, String.format("%d", CURRENT_FORMAT_VERSION));

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

  public static class Errors {
    public List<String> errorKeys = new ArrayList<>();
    public boolean lockingError = false;

    public void add(String key) {
      errorKeys.add(key);
    }
  }

  public static DatabaseConnection openDatabase(File path, Errors errors) {
    logger.info("openDatabase({})", path);
    DatabaseConnection newConnection = new DatabaseConnection();
    newConnection.lock = MyFileLock.lockFile(path);
    if (newConnection.lock == null) {
      errors.lockingError = true;
      errors.add("%DatabaseConnection.LockingError");
      return null;
    }
    newConnection.archive = DatabaseArchive.open(path);
    newConnection.originalArchiveFile = path;
    newConnection.loadOrCreateProperties();

    if (newConnection.archive == null) {
      errors.add("%DatabaseConnection.UnpackError");
      newConnection.close();
      return null;
    }
    if (!newConnection.connectToFile()) {
      errors.add("%DatabaseConnection.ConnectionError");
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

  public File getOriginalArchiveFile() {
    return originalArchiveFile;
  }

}
