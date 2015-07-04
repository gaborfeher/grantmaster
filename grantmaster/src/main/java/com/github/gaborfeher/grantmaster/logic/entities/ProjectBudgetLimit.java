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
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

@Entity
@Table(
    uniqueConstraints=
        @UniqueConstraint(columnNames={"budgetCategory_id", "project_id"}))
public class ProjectBudgetLimit extends EntityBase implements  Serializable {
  @Id
  @GeneratedValue
  private Long id;
  
  @Column(nullable = true, scale = 10, precision = 25)
  private BigDecimal budgetGrantCurrency;
  
  @Column(nullable = true, scale = 10, precision = 25)
  private BigDecimal budgetPercentage;
  
  @NotNull(message = "%ValidationErrorBudgetCategoryEmpty")
  @ManyToOne
  @JoinColumn(nullable = false)
  private BudgetCategory budgetCategory;
  
  @ManyToOne
  @JoinColumn(nullable = false)
  private Project project;
    
  public ProjectBudgetLimit() {
  }
  
  @AssertTrue(message="%ValidationErrorBudgetLimits")
  private boolean isValid() {
    if (budgetGrantCurrency == null && budgetPercentage == null) {
      return false;
    }
    if (budgetGrantCurrency != null &&
        budgetGrantCurrency.compareTo(BigDecimal.ZERO) < 0) {
      return false;
    }
    if (budgetPercentage != null &&
        budgetPercentage.compareTo(BigDecimal.ZERO) < 0) {
      return false;
    }
    return true;
  }

  public BigDecimal getBudgetGrantCurrency() {
    return budgetGrantCurrency;
  }

  public void setBudgetGrantCurrency(BigDecimal budget) {
    this.budgetGrantCurrency = budget;
  }

  public BudgetCategory getBudgetCategory() {
    return budgetCategory;
  }

  public void setBudgetCategory(BudgetCategory budgetCategory) {
    this.budgetCategory = budgetCategory;
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  public BigDecimal getBudgetPercentage() {
    return budgetPercentage;
  }

  public void setBudgetPercentage(BigDecimal budgetPercentage) {
    this.budgetPercentage = budgetPercentage;
  }

  @Override
  public Long getId() {
    return id;
  }
}
