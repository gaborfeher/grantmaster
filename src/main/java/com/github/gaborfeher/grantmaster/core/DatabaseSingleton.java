package com.github.gaborfeher.grantmaster.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
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
  private File tempDir;
  private boolean unsavedChanges;
  
  private DatabaseSingleton() {
  }
  
  public void cleanup() {
    close();
    if (tempDir != null) {
      simpleRecursiveDelete(tempDir);
    }
  }
    
  private void close() {
    unsavedChanges = false;
    if (entityManagerFactory != null) {
      entityManagerFactory.close();
      entityManagerFactory = null;
    }
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
  
  private static File createTempDir() {
    try {
      return Files.createTempDirectory("gmtmp").toFile();
    } catch (IOException ex) {
      Logger.getLogger(DatabaseSingleton.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }
  }
  
  public File createNewDatabase() {
    cleanup();
    tempDir = createTempDir();
    if (tempDir != null && connectToFile(tempDir.getAbsolutePath())) {
      return tempDir;
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
    Path tempFile = Files.createTempFile("gmsave", ".hdb");
    try (
        FileOutputStream fileOutputStream = new FileOutputStream(tempFile.toFile());
        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
      for (File item : tempDir.listFiles()) {
        ZipEntry entry = new ZipEntry(item.getName());
        zipOutputStream.putNextEntry(entry);
        zipOutputStream.write(Files.readAllBytes(item.toPath()));
        zipOutputStream.closeEntry();
      }
    }
    Files.move(tempFile, path.toPath(), StandardCopyOption.REPLACE_EXISTING);
    unsavedChanges = false;
    connectToFile(tempDir.getAbsolutePath());
    return tempDir;
  }
  
  /**
   * Returns the temporary directory where the database files are extracted.
   * @param path
   * @return 
   */
  public File openDatabase(File path) {
    cleanup();
    tempDir = createTempDir();
    if (tempDir == null) {
      return null;
    }
    try (
        FileInputStream fileInputStream = new FileInputStream(path);
        ZipInputStream zipInputStrem = new ZipInputStream(fileInputStream)) {
      ZipEntry zipEntry;
      while ((zipEntry = zipInputStrem.getNextEntry()) != null) {
        File fileToWrite = new File(tempDir, zipEntry.getName());
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
    connectToFile(tempDir.getAbsolutePath());
    return tempDir;
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
