/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.gaborfeher.grantmaster.core;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 *
 * @author gabor
 */
public class DatabaseConnectionSingleton {
  private static DatabaseConnectionSingleton instance;
  
  private EntityManagerFactory entityManagerFactory;
  private EntityManager entityManager;
  
  private DatabaseConnectionSingleton() {
    
  }
  
  public static DatabaseConnectionSingleton getInstance() {
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
    entityManager.getTransaction().begin();
    entityManager.persist(obj);
    entityManager.getTransaction().commit();
  }

  public void remove(Object obj) {
    entityManager.getTransaction().begin();
    entityManager.remove(obj);
    entityManager.getTransaction().commit();
  }
  
  public EntityManager em() {
    return entityManager;
  }

  public boolean isConnected() {
    return entityManager != null;
  }

}
