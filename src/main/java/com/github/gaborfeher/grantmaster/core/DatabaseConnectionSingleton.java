package com.github.gaborfeher.grantmaster.core;

import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import com.github.gaborfeher.grantmaster.ui.ControllerBase;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
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
import javax.persistence.TypedQuery;

public class DatabaseConnectionSingleton {
  private static DatabaseConnectionSingleton instance;
  
  private EntityManagerFactory entityManagerFactory;
 // private EntityManager entityManager;
  
  /**
   * Directory where the database files are stored while the database is open.
   */
  private File tempFile;
  
  private DatabaseConnectionSingleton() {
  }
  
  public static synchronized DatabaseConnectionSingleton getInstance() {
    if (instance == null) {
      instance = new DatabaseConnectionSingleton();
    }
    return instance;
  }
  
  public void close() {
 //   if (entityManager != null) {
 //     entityManager.close();
 //     entityManager = null;
 //   }
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
  
  public File createNewDatabase() {
    tempFile = createTempDir();
    System.out.println("tempFile= " + tempFile);
    if (connectTo(tempFile.getAbsolutePath())) {
      return tempFile;
    }
    return null;
  }
  
  private boolean connectTo(String pathString) {
    Map<String, String> properties = new HashMap<>();
    properties.put("javax.persistence.jdbc.url", "jdbc:hsqldb:file:" + new File(pathString, "database") +";shutdown=true");
    try {
      entityManagerFactory = Persistence.createEntityManagerFactory(
          "LocalH2ConnectionTemplate", properties);
   //   entityManager = entityManagerFactory.createEntityManager();
    } catch (PersistenceException ex) {
      return false;
    }
    System.out.println("Successful JPA connection");
    return true;
  }
  
  public File saveDatabase(File path) throws IOException {
    close();
    // TODO(gaborfeher): Is this enough to shut down?
    
    try (
        FileOutputStream fileOutputStream = new FileOutputStream(path);
        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
      for (File item : tempFile.listFiles()) {
        System.out.println("Adding: " + item.getAbsoluteFile());
        ZipEntry entry = new ZipEntry(item.getName());
        zipOutputStream.putNextEntry(entry);
        zipOutputStream.write(Files.readAllBytes(item.toPath()));
        zipOutputStream.closeEntry();
      }
    }
    
    connectTo(tempFile.getAbsolutePath());
    return tempFile;
  }
  
  /**
   * Returns the temporary directory where the database files are extracted.
   * @param path
   * @return 
   */
  public File openDatabase(File path) {
    close();
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
    connectTo(tempFile.getAbsolutePath());
    return tempFile;
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
        //hardReset();
      }
    });
  }
  /*
  public void hardReset() {
    // TODO: try to figure out when this is exactly needed
    entityManager.close();
    entityManager = entityManagerFactory.createEntityManager();
    //RefreshControlSingleton.getInstance().broadcastRefresh();
  }
  */
  
  public boolean runInTransaction(TransactionRunner runner) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
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
    } finally {
      entityManager.close();
    }
    runner.onSuccess();
    return true;
  }
  
  public void runWithEntityManager(TransactionRunner runner) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try  {
      runner.run(entityManager);
    } finally {
      entityManager.close();
    }
    
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
