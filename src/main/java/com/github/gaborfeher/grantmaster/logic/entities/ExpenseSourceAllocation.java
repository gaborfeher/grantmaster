package com.github.gaborfeher.grantmaster.logic.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Connects a ProjectExpense and a ProjectSource entity.
 * It means that a given amount of money from the source was
 * used to fulfill the expense. One expense has at least one
 * source, but it can have more. One source may have any number
 * of expenses.
 */
@Entity
@Table(
    uniqueConstraints=
        @UniqueConstraint(columnNames={"expense_id", "source_id"}))
public class ExpenseSourceAllocation implements Serializable {
  @Id
  @GeneratedValue
  private Long id;
  
  @ManyToOne
  @JoinColumn(nullable = false)
  private ProjectExpense expense;

  @ManyToOne
  @JoinColumn(nullable = false)
  private ProjectSource source;
  
  @Column(nullable = false, scale = 10, precision = 25)
  private BigDecimal accountingCurrencyAmount;
  
  public ExpenseSourceAllocation() {
  }

  public ProjectExpense getExpense() {
    return expense;
  }

  public void setExpense(ProjectExpense expense) {
    this.expense = expense;
  }

  public BigDecimal getAccountingCurrencyAmount() {
    return accountingCurrencyAmount;
  }

  public void setAccountingCurrencyAmount(BigDecimal amount) {
    this.accountingCurrencyAmount = amount;
  }

  public ProjectSource getSource() {
    return source;
  }

  public void setSource(ProjectSource source) {
    this.source = source;
  }  
}
