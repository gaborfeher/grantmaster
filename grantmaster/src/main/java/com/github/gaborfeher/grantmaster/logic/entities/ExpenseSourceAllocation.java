/**
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
