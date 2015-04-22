package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.entities.EntityBase;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectBudgetLimit;
import java.sql.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.persistence.EntityManager;

public class ProjectBudgetCategoryWrapper extends BudgetCategoryWrapper {
  private ProjectBudgetLimit limit;
  private Project project;
  
  public ProjectBudgetCategoryWrapper(BudgetCategory budgetCategory, Double spentAccountingCurrency, Double spentGrantCurrency) {
    super(budgetCategory);
    this.computedValues.put("spentGrantCurrency", spentGrantCurrency);
    this.computedValues.put("spentAccountingCurrency", spentAccountingCurrency);
    this.computedValues.put("remainingAccountingCurrency", null);
    this.computedValues.put("budgetAccountingCurrency", null);
  }
  
  public ProjectBudgetCategoryWrapper(String fakeName) {
    super(fakeName);
    this.computedValues.put("budgetCategory", fakeName);
    this.computedValues.put("spentGrantCurrency", 0.0);
    this.computedValues.put("spentAccountingCurrency", 0.0);
    this.computedValues.put("remainingAccountingCurrency", null);
    this.computedValues.put("budgetAccountingCurrency", null);

  }
  
  @Override
  public BudgetCategoryWrapper createFakeCopy(String fakeTitle) {
    BudgetCategoryWrapper copy = new ProjectBudgetCategoryWrapper(fakeTitle);
    copy.addSummaryValues(this, 1.0);
    copy.setIsSummary(true);
    copy.setState(null);
    return copy;
  }
  
  public void setProject(Project project) {
    this.project = project;
  }
  
  public void setLimit(double total, ProjectBudgetLimit limit) {
    this.limit = limit;
    if (limit == null) {
      return;
    }
    
    if (budgetCategory != null && !Objects.equals(budgetCategory.getId(), limit.getBudgetCategory().getId())) {
      System.out.println("bad expense type");
    }
    if (!Objects.equals(project.getId(), limit.getProject().getId())) {
      System.out.println("bad project for limit");
    }
    
    if (limit.getBudgetPercentage() != null) {
      limit.setBudgetGrantCurrency(total * limit.getBudgetPercentage() / 100.0);
    }
    if (limit.getBudgetGrantCurrency() != null) {
      double remainingGrantCurrency = limit.getBudgetGrantCurrency();
      Double spentGrantCurrency = (Double) computedValues.get("spentGrantCurrency");
      if (spentGrantCurrency != null) {
        remainingGrantCurrency -= spentGrantCurrency;
      }
      this.computedValues.put("remainingGrantCurrency", remainingGrantCurrency);
    }
  }

  public void setBudgetCategory(BudgetCategory budgetCategory) {
    limit.setBudgetCategory(budgetCategory);
    this.budgetCategory = budgetCategory;
  }

  @Override
  public void addSummaryValues(BudgetCategoryWrapper other, double multiplier) {
    addSummaryValue(other, "spentGrantCurrency", multiplier);
    addSummaryValue(other, "spentAccountingCurrency", multiplier);
  }
  
  public void addBudgetAmounts(double accountingCurrencyAmount, double grantCurrencyAmount) {
    if (limit == null) {
      limit = new ProjectBudgetLimit();
    }
    if (limit.getBudgetGrantCurrency() == null) {
      limit.setBudgetGrantCurrency(grantCurrencyAmount);
    } else {
      limit.setBudgetGrantCurrency(limit.getBudgetGrantCurrency() + grantCurrencyAmount);
    }
    Double budgetAccountingCurrency = (Double) computedValues.get("budgetAccountingCurrency");
    if (budgetAccountingCurrency == null) {
      budgetAccountingCurrency = 0.0;
    }
    budgetAccountingCurrency += accountingCurrencyAmount;
    computedValues.put("budgetAccountingCurrency", budgetAccountingCurrency);
    computedValues.put("remainingGrantCurrency", limit.getBudgetGrantCurrency() - (Double)computedValues.get("spentGrantCurrency"));
    computedValues.put("remainingAccountingCurrency", budgetAccountingCurrency - (Double)computedValues.get("spentAccountingCurrency"));
  }
  
  @Override
  protected EntityBase getEntity() {
    return limit;
  }
  
  @Override
  public void setState(State state) {
    if (state == State.EDITING) {
      if (limit == null) {
        state = State.EDITING_NEW;
        limit = new ProjectBudgetLimit();
        limit.setBudgetCategory(budgetCategory);
        limit.setProject(project);
      }
    }
    super.setState(state);
  }
  
  public static List<ProjectBudgetCategoryWrapper> getProjectBudgetLimits(
      EntityManager em,
      Project project,
      Date filterStartDate,
      Date filterEndDate) {
    Double total =
        Utils.getSingleResultWithDefault(0.0,
            em.createQuery("SELECT SUM(s.amount) FROM ProjectSource s WHERE s.project = :project GROUP BY s.project", Double.class).
                setParameter("project", project));

        
    List<ProjectBudgetCategoryWrapper> list = em.createQuery("SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectBudgetCategoryWrapper(c, SUM(a.accountingCurrencyAmount), SUM(a.accountingCurrencyAmount / s.exchangeRate)) " +
        "FROM BudgetCategory c LEFT OUTER JOIN ProjectExpense e ON e.budgetCategory = c AND e.project = :project LEFT OUTER JOIN ExpenseSourceAllocation a ON a.expense = e LEFT OUTER JOIN ProjectSource s ON a.source = s AND s.project = :project " +
            "WHERE c.direction = :direction " +
            " AND (:filterStartDate IS NULL OR e.paymentDate >= :filterStartDate) " +
            " AND (:filterEndDate IS NULL OR e.paymentDate <= :filterEndDate) " +
            "GROUP BY c " +
            "ORDER BY c.groupName NULLS LAST, c.name",
        ProjectBudgetCategoryWrapper.class).
            setParameter("project", project).
            setParameter("direction", BudgetCategory.Direction.PAYMENT).
            setParameter("filterStartDate", filterStartDate).
            setParameter("filterEndDate", filterEndDate).
            getResultList();
    
    Iterator<ProjectBudgetCategoryWrapper> iterator = list.iterator();
    while (iterator.hasNext()) {
      ProjectBudgetCategoryWrapper limitWrapper = iterator.next();
      
      limitWrapper.setProject(project);
      ProjectBudgetLimit limit = 
          Utils.getSingleResultWithDefault(
              null,
              em.createQuery(
                  "SELECT l FROM ProjectBudgetLimit l WHERE l.project = :project AND l.budgetCategory = :budgetCategory",
                  ProjectBudgetLimit.class).
                  setParameter("project", project).
                  setParameter("budgetCategory", limitWrapper.getBudgetCategory()));


      limitWrapper.setLimit(total, limit);
      if (limitWrapper.getEntity() == null && limitWrapper.computedValues.get("spentGrantCurrency") == null) {
        iterator.remove();
      }
    }

    return list;
  }
  
  static void removeProjectBudgetLimits(EntityManager em, Project project) {
    em.createQuery("DELETE FROM ProjectBudgetLimit l WHERE l.project = :project").
        setParameter("project", project).
        executeUpdate();
  }

  @Override
  protected void setEntity(EntityBase entity) {
    this.limit = (ProjectBudgetLimit) entity;
  }


}
