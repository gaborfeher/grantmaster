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
package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.framework.base.EntityWrapper;
import com.github.gaborfeher.grantmaster.framework.utils.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.framework.base.EntityBase;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;

public abstract class BudgetCategoryWrapperBase<T extends EntityBase> extends EntityWrapper<T> {
  private final String fakeName;

  public BudgetCategoryWrapperBase(T entity, String fakeName) {
    super(entity);
    this.fakeName = fakeName;
  }

  public static BudgetCategoryWrapperBase createBudgetSummaryList(
      EntityManager em,
      List<BudgetCategoryWrapperBase> rawLines,
      String summaryTitle,
      List<BudgetCategoryWrapperBase> summary) {
    BudgetCategoryWrapperBase totalSum = null;
    BudgetCategoryWrapperBase groupSum = null;
    String currentGroupName = null;
    BudgetCategoryWrapperBase previous = null;
    for (BudgetCategoryWrapperBase current : rawLines) {
      if (previous != null) {
        if (currentGroupName != null && !currentGroupName.equals(current.getGroupName())) {
          summary.add(groupSum);
          currentGroupName = null;
          groupSum = null;
        }
      }
      if (current.getGroupName() != null) {
        if (currentGroupName == null || groupSum == null) {
          currentGroupName = current.getGroupName();
          groupSum = current.createFakeCopy(
              current.getGroupName() + " " + Utils.getString("Summary.TotalSuffix"));
        } else {
          groupSum.addSummaryValues(current, BigDecimal.ONE);
        }
      }
      summary.add(current);
      if (totalSum != null) {
        totalSum.addSummaryValues(current, BigDecimal.ONE);
      } else {
        totalSum = current.createFakeCopy(summaryTitle);
      }
      previous = current;
    }
    if (groupSum != null) {
      summary.add(groupSum);
    }
    if (totalSum != null) {
      summary.add(totalSum);
    }
    return totalSum;
  }

  /**
   * Processes the lists of income and expense budget categories, and inserts
   * summary lines after groups of categories.
   * @param paymentCategories
   * @param incomeCategories
   * @param output
   */
  public static void createBudgetSummaryList(
      EntityManager em,
      List<BudgetCategoryWrapperBase> paymentCategories,
      List<BudgetCategoryWrapperBase> incomeCategories,
      Set<String> excludeColumnsFromFinal,
      List<BudgetCategoryWrapperBase> output) {
    BudgetCategoryWrapperBase expenseSum = createBudgetSummaryList(
        em, paymentCategories, Utils.getString("Summary.ExpenseSummary"), output);
    BudgetCategoryWrapperBase incomeSum = createBudgetSummaryList(
        em, incomeCategories, Utils.getString("Summary.IncomeSummary"), output);
    BudgetCategoryWrapperBase finalSum = incomeSum.createFakeCopy(
        Utils.getString("Summary.Difference"));
    if (expenseSum != null) {
      finalSum.addSummaryValues(expenseSum, new BigDecimal(-1));
    }
    finalSum.removeColumns(excludeColumnsFromFinal);
    output.add(finalSum);
  }

  public String getGroupName() {
    if (getBudgetCategory() == null) {
      return null;
    }
    return getBudgetCategory().getGroupName();
  }

  public abstract BudgetCategory getBudgetCategory();

  @Override
  public Object getProperty(String name) {
    if (fakeName != null &&
            ("name".equals(name) || "budgetCategory".equals(name))) {
      return fakeName;
    }
    return super.getProperty(name);
  }

  protected abstract void addSummaryValues(BudgetCategoryWrapperBase current, BigDecimal multiplier);
  protected abstract BudgetCategoryWrapperBase createFakeCopy(String summaryTitle);

  protected abstract void removeColumns(Set<String> columns);

}
