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
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import org.slf4j.LoggerFactory;

/**
 * Stores a DatabaseConnection and provides convenience methods for using it.
 */
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
    if (this.connection != null) {
      this.connection.close();
    }
    this.connection = connection;
  }
  
  public boolean runOrRollback(TransactionRunner runner, EntityManager em) {
    if (runner.run(em)) {
      return true;
    } else {
      em.getTransaction().rollback();
      return false;
    }
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
        if (transaction.isActive()) {  // It may already be rolled back.
            transaction.rollback();
        }
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

  public void close() {
    if (connection != null) {
      connection.close();
    }
  }

  public boolean saveDatabase() {
    if (connection != null) {
      return connection.saveDatabase();
    } else {
      return false;
    }
  }
  
  public boolean saveAsDatabase(File path) {
    if (connection != null) {
      return connection.saveAsDatabase(path);
    } else {
      return false;
    }
  }
  
  public File getCurrentlyOpenArchiveFile() {
    return connection.getOriginalArchiveFile();
  }

  public boolean connectToMemoryFileForTesting() {
    connection = DatabaseConnection.createNewMemoryDatabaseForTesting();
    return connection != null;
  }
}
