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
  public BigDecimal getAccountingCurrencyAmount() {
    return accountingCurrencyAmount;
  }

  /**
   * @param amount the amount to set
   */
  public void setAccountingCurrencyAmount(BigDecimal amount) {
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
