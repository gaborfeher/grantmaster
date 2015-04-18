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
  private Double budgetGrantCurrency;
  private Double budgetAccountingCurrency;
  private double spentGrantCurrency;
  private double spentAccountingCurrency;
  private final boolean enableNumbers;
  private final String groupName;
  
  public FakeBudgetEntityWrapper(String title, boolean enableNumbers, String groupName) {
    this.title = title;
    this.enableNumbers = enableNumbers;
    this.groupName = groupName;
  }
  
  public FakeBudgetEntityWrapper(String title, boolean enableNumbers) {
    this.title = title;
    this.enableNumbers = enableNumbers;
    this.groupName = null;
  }
  
  public FakeBudgetEntityWrapper(String title) {
    this.title = title;
    this.enableNumbers = false;
    this.groupName = null;
  }
  
  public String getExpenseType() {
    return title;
  }
  
  public Double getBudgetGrantCurrency() {
    if (!enableNumbers) {
      return null;
    }
    return budgetGrantCurrency;
  }
  
  public Double getBudgetAccountingCurrency() {
    if (!enableNumbers) {
      return null;
    }
    return budgetAccountingCurrency;
  }

  public Double getSpentGrantCurrency() {
    if (!enableNumbers) {
      return null;
    }
    return spentGrantCurrency;
  }
  
  public Double getRemainingGrantCurrency() {
    if (!enableNumbers) {
      return null;
    }
    if (budgetGrantCurrency == null) {
      return null;
    }
    return budgetGrantCurrency - spentGrantCurrency;
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
    if (budgetLine.getSpentGrantCurrency() != null) {
      setSpentGrantCurrency(getSpentGrantCurrency() + budgetLine.getSpentGrantCurrency());
    }
    if (budgetLine.getSpentAccountingCurrency() != null) {
      setSpentAccountingCurrency(getSpentAccountingCurrency() + budgetLine.getSpentAccountingCurrency());
    }
    if (budgetLine.getBudgetGrantCurrency() != null) {
      if (getBudgetGrantCurrency() == null) {
        setBudgetGrantCurrency(0.0);
      }
      setBudgetGrantCurrency(getBudgetGrantCurrency() + budgetLine.getBudgetGrantCurrency());
    }
  }

  public void add(FakeBudgetEntityWrapper budgetLine) {
    // TODO(gaborfeher): Unify with above.
    if (budgetLine.getSpentGrantCurrency() != null) {
      setSpentGrantCurrency(getSpentGrantCurrency() + budgetLine.getSpentGrantCurrency());
    }
    if (budgetLine.getSpentAccountingCurrency() != null) {
      setSpentAccountingCurrency(getSpentAccountingCurrency() + budgetLine.getSpentAccountingCurrency());
    }
    if (budgetLine.getBudgetGrantCurrency() != null) {
      if (getBudgetGrantCurrency() == null) {
        setBudgetGrantCurrency(0.0);
      }
      setBudgetGrantCurrency(getBudgetGrantCurrency() + budgetLine.getBudgetGrantCurrency());
    }
    
    
    if (budgetLine.getBudgetAccountingCurrency() != null) {
      if (getBudgetAccountingCurrency() == null) {
        setBudgetAccountingCurrency(0.0);
      }
      setBudgetAccountingCurrency(getBudgetAccountingCurrency() + budgetLine.getBudgetAccountingCurrency());
    }
  }
  

  /**
   * @param budget the budget to set
   */
  public void setBudgetGrantCurrency(double budget) {
    this.budgetGrantCurrency = budget;
  }

  /**
   * @param spent the spent to set
   */
  public void setSpentGrantCurrency(double spent) {
    this.spentGrantCurrency = spent;
  }

  /**
   * @return the spentAccountingCurrency
   */
  public Double getSpentAccountingCurrency() {
    if (!enableNumbers) {
      return null;
    }
    return spentAccountingCurrency;
  }
  
  public Double getRemainingAccountingCurrency() {
    if (!enableNumbers) {
      return null;
    }
    if (budgetAccountingCurrency == null) {
      return null;
    }
    return budgetAccountingCurrency - spentAccountingCurrency;
  }

  public void setSpentAccountingCurrency(double spentAccountingCurrency) {
    this.spentAccountingCurrency = spentAccountingCurrency;
  }

  public void setBudgetAccountingCurrency(double budgetAccountingCurrency) {
    this.budgetAccountingCurrency = budgetAccountingCurrency;
  }

  public String getGroupName() {
    return groupName;
  }

  
}
