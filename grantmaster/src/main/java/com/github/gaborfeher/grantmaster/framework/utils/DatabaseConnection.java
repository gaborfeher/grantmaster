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
import java.util.HashMap;
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
  }
    
  private void disconnect() {
    unsavedChanges = false;
    if (getEntityManagerFactory() != null) {
      getEntityManagerFactory().close();
      entityManagerFactory = null;
    }
  }
    
  private void loadProperties() {
    File propertiesFile = new File(archive.getDirectory(), PROPERTIES_FILE);
    properties = new Properties();
    try {
      if (propertiesFile.exists() && !propertiesFile.isDirectory()) {
        properties.load(new FileReader(propertiesFile));
      }
    } catch (IOException ex) {
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
  
  private boolean checkProperties() {
    final int CURRENT_FORMAT_VERSION = 1;
    final String FORMAT_VERSION = "format.version";
    if (!properties.containsKey(FORMAT_VERSION)) {
      properties.put(FORMAT_VERSION, String.format("%d", CURRENT_FORMAT_VERSION));
      return true;
    }
    int formatVersion = Integer.parseInt((String) properties.get(FORMAT_VERSION));
    switch (formatVersion) {
      case CURRENT_FORMAT_VERSION:
        return true;
      // Earlier versions can be handled here.
      default:
        return false;
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
    File databaseFile = new File(archive.getDirectory(), "database");
    if (!connectToJdbcUrl("jdbc:hsqldb:file:" + databaseFile +";shutdown=true")) {
      return false;
    }
    loadProperties();
    return checkProperties();
  }
  
  public void saveDatabase(File path) throws IOException {
    logger.info("saveDatabase({})", path);
    disconnect();
    storeProperties();
    archive.saveToArchiveFile(path);
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
