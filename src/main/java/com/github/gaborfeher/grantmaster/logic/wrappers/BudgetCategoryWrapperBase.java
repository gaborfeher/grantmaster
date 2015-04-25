package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import java.math.BigDecimal;
import java.util.List;
import javax.persistence.EntityManager;

public abstract class BudgetCategoryWrapperBase extends EntityWrapper {
  protected BudgetCategory budgetCategory;
  private final String fakeName;
  
  public BudgetCategoryWrapperBase(BudgetCategory budgetCategory, String fakeName) {
    this.budgetCategory = budgetCategory;
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
          groupSum = current.createFakeCopy(current.getGroupName() + " mind\u00f6sszesen");
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
      List<BudgetCategoryWrapperBase> output) {
    BudgetCategoryWrapperBase expenseSum = createBudgetSummaryList(em, paymentCategories, "K\u00f6lts\u00e9gek mind\u00f6sszesen", output);
    BudgetCategoryWrapperBase incomeSum = createBudgetSummaryList(em, incomeCategories, "Bev\u00e9telek mind\u00f6sszesen", output);
    BudgetCategoryWrapperBase finalSum = incomeSum.createFakeCopy("K\u00fcl\u00f6nbs\u00e9g");
    if (expenseSum != null) {
      finalSum.addSummaryValues(expenseSum, new BigDecimal(-1));
    }
    output.add(finalSum);
  }

  public Long getId() {
    return budgetCategory.getId();
  }
  
  public String getGroupName() {
    if (budgetCategory == null) {
      return null;
    }
    return budgetCategory.getGroupName();
  }
   
  public BudgetCategory getBudgetCategory() {
    return budgetCategory;
  }

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
  
}
