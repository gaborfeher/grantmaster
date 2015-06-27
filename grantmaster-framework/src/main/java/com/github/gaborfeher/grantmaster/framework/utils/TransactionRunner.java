package com.github.gaborfeher.grantmaster.framework.utils;

import javax.persistence.EntityManager;

public interface TransactionRunner {
  public boolean run(EntityManager em);
}
