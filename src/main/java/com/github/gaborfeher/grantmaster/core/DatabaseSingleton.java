package com.github.gaborfeher.grantmaster.core;

import com.github.gaborfeher.grantmaster.logic.entities.EntityBase;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

public enum DatabaseSingleton {
  INSTANCE;
  
  private EntityManagerFactory entityManagerFactory;
 // private EntityManager entityManager;
  
  /**
   * Directory where the database files are stored while the database is open.
   */
  private File tempFile;
  
  private DatabaseSingleton() {
  }
  
  public void cleanup() {
    close();
    if (tempFile != null) {
      simpleRecursiveDelete(tempFile);
    }
  }
    
  private void close() {
    if (entityManagerFactory != null) {
      entityManagerFactory.close();
      entityManagerFactory = null;
    }
  }
  
  // This is copied from Guava. TODO: include Guava in the project
  private static final int TEMP_DIR_ATTEMPTS = 10000;
  private File createTempDir() {
    File baseDir = new File(System.getProperty("java.io.tmpdir"));
    String baseName = System.currentTimeMillis() + "-";

    for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
      File tempDir = new File(baseDir, baseName + counter);
      if (tempDir.mkdir()) {
        return tempDir;
      }
    }
    throw new IllegalStateException("Failed to create directory within "
        + TEMP_DIR_ATTEMPTS + " attempts (tried "
       + baseName + "0 to " + baseName + (TEMP_DIR_ATTEMPTS - 1) + ')');
  }
  
  private static void simpleRecursiveDelete(File main) {
    // No simlink handling.
    if (main.isDirectory()) {
      for (File sub : main.listFiles()) {
        simpleRecursiveDelete(sub);
      }
    }
    if (!main.delete()) {
      System.out.println("failed to delete " + main);
    }
  }
  
  public File createNewDatabase() {
    cleanup();
    tempFile = createTempDir();
    if (connectToFile(tempFile.getAbsolutePath())) {
      return tempFile;
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
  
  private boolean connectToFile(String pathString) {
    return connectToJdbcUrl("jdbc:hsqldb:file:" + new File(pathString, "database") +";shutdown=true");
  }
  
  public File saveDatabase(File path) throws IOException {
    close();
    // TODO(gaborfeher): Is this enough to shut down?
    
    try (
        FileOutputStream fileOutputStream = new FileOutputStream(path);
        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
      for (File item : tempFile.listFiles()) {
        ZipEntry entry = new ZipEntry(item.getName());
        zipOutputStream.putNextEntry(entry);
        zipOutputStream.write(Files.readAllBytes(item.toPath()));
        zipOutputStream.closeEntry();
      }
    }
    
    connectToFile(tempFile.getAbsolutePath());
    return tempFile;
  }
  
  /**
   * Returns the temporary directory where the database files are extracted.
   * @param path
   * @return 
   */
  public File openDatabase(File path) {
    cleanup();
    tempFile = createTempDir();
    try (
        FileInputStream fileInputStream = new FileInputStream(path);
        ZipInputStream zipInputStrem = new ZipInputStream(fileInputStream)) {
      ZipEntry zipEntry;
      while ((zipEntry = zipInputStrem.getNextEntry()) != null) {
        File fileToWrite = new File(tempFile, zipEntry.getName());
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileToWrite)) {
          while (zipInputStrem.available() > 0) {
            byte[] buffer = new byte[1024 * 32];
            int len = zipInputStrem.read(buffer);
            if (len > 0) {
              fileOutputStream.write(buffer, 0, len);
            }
          }
        } catch (IOException e) {
          return null;
        }
      }
    } catch (IOException e) {
      return null;
    }
    connectToFile(tempFile.getAbsolutePath());
    return tempFile;
  }
  
  public void persist(final Object obj) {
    transaction(new TransactionRunner() {
      @Override
      public boolean run(EntityManager em) {
        em.persist(obj);
        return true;
      }
    });
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
        //runner.onFailure();
        return false;
      }
    } catch (Throwable t) {
      Logger.getLogger(DatabaseSingleton.class.getName()).log(Level.SEVERE, null, t);
      if (transaction.isActive()) {
        transaction.rollback();
      }
      //runner.onFailure();
      return false;
    } finally {
      entityManager.close();
    }
    //runner.onSuccess();
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

  public void refresh(Object entity) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    entityManager.refresh(entity);
    entityManager.close();
  }
}
