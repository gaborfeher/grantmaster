package com.github.gaborfeher.grantmaster.core;

import javax.persistence.EntityManager;

public interface TransactionRunner {
  public boolean run(EntityManager em);
}
