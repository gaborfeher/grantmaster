/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.gaborfeher.grantmaster.logic.wrappers;

/**
 *
 * @author gabor
 */
public class FakeBudgetEntityWrapper extends EntityWrapper {
  private final String title;
  private double budget;
  private double spent;
  private double remaining;
  private final boolean enableNumbers;
  
  public FakeBudgetEntityWrapper(String title, boolean enableNumbers) {
    this.title = title;
    this.enableNumbers = enableNumbers;
  }
  
  public FakeBudgetEntityWrapper(String title) {
    this.title = title;
    this.enableNumbers = false;
  }
  
  public String getExpenseType() {
    return title;
  }
  
  public Double getBudget() {
    if (!enableNumbers) {
      return null;
    }
    return budget;
  }

  public Double getSpent() {
    if (!enableNumbers) {
      return null;
    }
    return spent;
  }
  
  public Double getRemaining() {
    if (!enableNumbers) {
      return null;
    }
    return remaining;
  }
  
  @Override
  public boolean isFake() {
    return true;
  }
  
  @Override
  public boolean canEdit() {
    return false;
  }
  
  @Override
  protected Object getEntity() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  public void add(ProjectBudgetLimitWrapper budgetLine) {
   // if (budgetLine.getEntity() == null) {
   //   return;
   // }
    spent += budgetLine.getSpent();
    if (budgetLine.getRemaining() != null) {
      remaining += budgetLine.getRemaining();
    }
    if (budgetLine.getBudget() != null) {
      budget += budgetLine.getBudget();
    }
  }
  
}
