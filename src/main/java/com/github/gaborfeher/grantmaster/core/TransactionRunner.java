package com.github.gaborfeher.grantmaster.core;

import javax.persistence.EntityManager;

public abstract class TransactionRunner {
  public abstract boolean run(EntityManager em);

  public void onFailure() {
  }

  public void onSuccess() {
  }
}
