package com.github.gaborfeher.grantmaster.core;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

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
  
  public void connectTo(String pathString) {
    Map<String, String> properties = new HashMap<>();
    properties.put("javax.persistence.jdbc.url", "jdbc:h2:" + pathString);
    entityManagerFactory = Persistence.createEntityManagerFactory(
        "LocalH2ConnectionTemplate", properties);
    entityManager = entityManagerFactory.createEntityManager();
    System.out.println("Successful JPA connection");
  }
  
  public void persist(Object obj) {
  //  try {
      entityManager.getTransaction().begin();
      entityManager.persist(obj);
      entityManager.getTransaction().commit();
  //  } catch (Throwable t) {
  //    Logger.getLogger(DatabaseConnectionSingleton.class.getName()).log(Level.SEVERE, null, t);
  //    cleanup();
  //  }
  }

  public void remove(Object obj) {
    EntityTransaction transaction = entityManager.getTransaction();
    try {
      transaction.begin();
      entityManager.remove(obj);
      transaction.commit();
    } catch (Throwable t) {
      Logger.getLogger(DatabaseConnectionSingleton.class.getName()).log(Level.SEVERE, null, t);
      hardReset();
    }
  }
  
  public void hardReset() {
    entityManager.close();
    entityManager = entityManagerFactory.createEntityManager();
    RefreshControlSingleton.getInstance().broadcastRefresh(null);
  }
  
  public EntityManager em() {
    return entityManager;
  }

  public boolean isConnected() {
    return entityManager != null;
  }

}
