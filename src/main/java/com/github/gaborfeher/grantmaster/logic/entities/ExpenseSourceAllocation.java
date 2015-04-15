package com.github.gaborfeher.grantmaster.logic.entities;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
    uniqueConstraints=
        @UniqueConstraint(columnNames={"expense_id", "source_id"}))
public class ExpenseSourceAllocation implements Serializable {
  @Id
  @GeneratedValue
  private int id;
  
  @ManyToOne
  private ProjectExpense expense;

  @ManyToOne
  private ProjectSource source;
  
  private double accountingCurrencyAmount;
  
  public ExpenseSourceAllocation() {
  }

  /**
   * @return the expense
   */
  public ProjectExpense getExpense() {
    return expense;
  }

  /**
   * @param expense the expense to set
   */
  public void setExpense(ProjectExpense expense) {
    this.expense = expense;
  }

  /**
   * @return the amount
   */
  public double getAccountingCurrencyAmount() {
    return accountingCurrencyAmount;
  }

  /**
   * @param amount the amount to set
   */
  public void setAccountingCurrencyAmount(double amount) {
    this.accountingCurrencyAmount = amount;
  }

  /**
   * @return the source
   */
  public ProjectSource getSource() {
    return source;
  }

  /**
   * @param source the source to set
   */
  public void setSource(ProjectSource source) {
    this.source = source;
  }

  
  
}
