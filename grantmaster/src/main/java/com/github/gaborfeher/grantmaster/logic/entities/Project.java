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
package com.github.gaborfeher.grantmaster.logic.entities;

import com.github.gaborfeher.grantmaster.framework.base.EntityBase;
import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class Project extends EntityBase implements  Serializable {
  @Id
  @GeneratedValue
  private Long id;

  @NotNull(message = "%ValidationErrorNameEmpty")
  @Size(min = 1, message="%ValidationErrorNameEmpty")
  @Column(nullable = false, unique = true)
  private String name;

  @NotNull(message = "%ValidationErrorGrantCurrencyEmpty")
  @ManyToOne
  @JoinColumn(nullable = false)
  private Currency grantCurrency;

  @NotNull(message = "%ValidationErrorAccountingCurrencyEmpty")
  @ManyToOne
  @JoinColumn(nullable = false)
  private Currency accountCurrency;

  @NotNull(message = "%ValidationErrorIncomeCategoryEmpty")
  @ManyToOne
  @JoinColumn(nullable = false)
  private BudgetCategory incomeType;

  @OneToMany(mappedBy = "project")
  private List<ProjectReport> reports;

  public static enum ExpenseMode {
    NORMAL_AUTO_BY_SOURCE,
    OVERRIDE_AUTO_BY_RATE_TABLE;
  }
  @Column(nullable = false)
  private ExpenseMode expenseMode = ExpenseMode.NORMAL_AUTO_BY_SOURCE;

  public Project() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public String toString() {
    return getName();
  }

  public Currency getGrantCurrency() {
    return grantCurrency;
  }

  public void setGrantCurrency(Currency grantCurrency) {
    this.grantCurrency = grantCurrency;
  }

  public Currency getAccountCurrency() {
    return accountCurrency;
  }

  public void setAccountCurrency(Currency accountCurrency) {
    this.accountCurrency = accountCurrency;
  }

  public BudgetCategory getIncomeType() {
    return incomeType;
  }

  public void setIncomeType(BudgetCategory incomeType) {
    this.incomeType = incomeType;
  }

  public List<ProjectReport> getReports() {
    return reports;
  }

  public void setReports(List<ProjectReport> reports) {
    this.reports = reports;
  }

  public ExpenseMode getExpenseMode() {
    return expenseMode;
  }

  public void setExpenseMode(ExpenseMode expenseMode) {
    this.expenseMode = expenseMode;
  }

}
