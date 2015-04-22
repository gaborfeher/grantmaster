package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.core.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectBudgetLimit;
import com.github.gaborfeher.grantmaster.ui.ControllerBase;
import java.sql.Date;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;

public class ProjectBudgetCategoryWrapper extends BudgetCategoryWrapper {
  private ProjectBudgetLimit limit;
  private Double budgetAccountingCurrency;  // Only for display purposes in summary view.
  private Double spentGrantCurrency;
  private Double spentAccountingCurrency;
  private Double remainingGrantCurrency;
  private Double remainingAccountingCurrency;  // Only for display purposes in summary view.
  private Project project;
  
  public ProjectBudgetCategoryWrapper(BudgetCategory budgetCategory, Double spentAccountingCurrency, Double spentGrantCurrency) {
    super(budgetCategory);
    this.spentGrantCurrency = spentGrantCurrency;
    this.spentAccountingCurrency = spentAccountingCurrency;
    this.remainingGrantCurrency = null;
    this.budgetAccountingCurrency = null;
    this.remainingAccountingCurrency = null;
  }
  
  public ProjectBudgetCategoryWrapper(String fakeTitle) {
    super(fakeTitle);
    this.spentAccountingCurrency = 0.0;
    this.spentGrantCurrency = 0.0;
    this.remainingGrantCurrency = null;
    this.budgetAccountingCurrency = null;
    this.remainingAccountingCurrency = null;
  }
  
  @Override
  public BudgetCategoryWrapper createFakeCopy(String fakeTitle) {
    BudgetCategoryWrapper copy = new ProjectBudgetCategoryWrapper(fakeTitle);
    copy.addSummaryValues(this, 1.0);
    copy.setIsSummary(true);
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
    
    if (budgetCategory != null && budgetCategory.getId() != limit.getBudgetCategory().getId()) {
      System.out.println("bad expense type");
    }
    if (project.getId() != limit.getProject().getId()) {
      System.out.println("bad project for limit");
    }
    
    if (limit.getBudgetPercentage() != null) {
      limit.setBudget(total * limit.getBudgetPercentage() / 100.0);
    }
    if (limit.getBudget() != null) {
      this.remainingGrantCurrency = limit.getBudget();
      if (spentGrantCurrency != null) {
        this.remainingGrantCurrency -= spentGrantCurrency;
      }
    }
  }
  
  public Double getSpentAccountingCurrency() {
    return spentAccountingCurrency;
  }
  
  public Double getBudgetGrantCurrency() {
    if (limit == null) {
      return null;
    }
    return limit.getBudget();
  }
  
  public Double getBudgetAccountingCurrency() {
    return budgetAccountingCurrency;
  }
  
  public void setBudgetGrantCurrency(Double budget) {
    limit.setBudget(budget);
    remainingGrantCurrency = limit.getBudget();
    if (spentGrantCurrency != null) {
      remainingGrantCurrency -= spentGrantCurrency;
    }
  }
  
  public Double getSpentGrantCurrency() {
    return spentGrantCurrency;
  }
  
  public Double getRemainingGrantCurrency() {
    return remainingGrantCurrency;
  }
  
  public Double getRemainingAccountingCurrency() {
    return remainingAccountingCurrency;
  }

  public void setSpentGrantCurrency(double spentGrantCurrency) {
    this.spentGrantCurrency = spentGrantCurrency;
  }
  
  public void setSpentAccountingCurrency(double spentAccountingCurrency) {
    this.spentAccountingCurrency = spentAccountingCurrency;
  }

  public void setBudgetCategory(BudgetCategory budgetCategory) {
    limit.setBudgetCategory(budgetCategory);
    this.budgetCategory = budgetCategory;
  }
  
  public Double getBudgetPercentage() {
    if (limit == null) {
      return null;
    }
    return limit.getBudgetPercentage();
  }
  
  public void setBudgetPercentage(Double budgetPercentage) {
    limit.setBudgetPercentage(budgetPercentage);
  }
  
  @Override
  public void addSummaryValues(BudgetCategoryWrapper other, double multiplier) {
    ProjectBudgetCategoryWrapper budgetLine = (ProjectBudgetCategoryWrapper) other;
    if (budgetLine.getSpentGrantCurrency() != null) {
      spentGrantCurrency = getSpentGrantCurrency() +
          budgetLine.getSpentGrantCurrency() * multiplier;
    }
    if (budgetLine.getSpentAccountingCurrency() != null) {
      spentAccountingCurrency = getSpentAccountingCurrency() +
          budgetLine.getSpentAccountingCurrency() * multiplier;
    }
  }
  
  /*
  public void addAmounts(double grantCurrencyAmount, double accountingCurrencyAmount) {
    this.spentGrantCurrency = (this.spentGrantCurrency == null ? 0.0 : this.spentAccountingCurrency) + grantCurrencyAmount;
    this.spentAccountingCurrency = (this.spentAccountingCurrency == null ? 0.0 : this.spentAccountingCurrency) + accountingCurrencyAmount;
  }
  */
  
  public void addBudgetAmounts(double accountingCurrencyAmount, double grantCurrencyAmount) {
    if (limit == null) {
      limit = new ProjectBudgetLimit();
    }
    if (limit.getBudget() == null) {
      limit.setBudget(grantCurrencyAmount);
    } else {
      limit.setBudget(limit.getBudget() + grantCurrencyAmount);
    }
    if (budgetAccountingCurrency == null) {
      budgetAccountingCurrency = accountingCurrencyAmount;
    } else {
      budgetAccountingCurrency += accountingCurrencyAmount;
    }
    this.remainingGrantCurrency = limit.getBudget() - spentGrantCurrency;
    this.remainingAccountingCurrency = budgetAccountingCurrency - spentAccountingCurrency;
  }
  
  @Override
  protected Object getEntity() {
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
      Project project,
      Date filterStartDate,
      Date filterEndDate,
      ControllerBase parent) {
    DatabaseConnectionSingleton conn = DatabaseConnectionSingleton.getInstance();
    Double total = 
        Utils.getSingleResultWithDefault(0.0,
            conn.createQuery("SELECT SUM(s.amount) FROM ProjectSource s WHERE s.project = :project GROUP BY s.project", Double.class).
            setParameter("project", project));
        
    List<ProjectBudgetCategoryWrapper> list = EntityWrapper.createQuery("SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectBudgetCategoryWrapper(c, SUM(a.accountingCurrencyAmount), SUM(a.accountingCurrencyAmount / s.exchangeRate)) " +
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
            getResultList(parent);
    
    Iterator<ProjectBudgetCategoryWrapper> iterator = list.iterator();
    while (iterator.hasNext()) {
      ProjectBudgetCategoryWrapper limitWrapper = iterator.next();
      
      limitWrapper.setProject(project);
      ProjectBudgetLimit limit = Utils.getSingleResultWithDefault(null, conn.createQuery(
          "SELECT l FROM ProjectBudgetLimit l WHERE l.project = :project AND l.budgetCategory = :budgetCategory",
          ProjectBudgetLimit.class).
          setParameter("project", project).
          setParameter("budgetCategory", limitWrapper.getBudgetCategory()));

      limitWrapper.setLimit(total, limit);
      if (limitWrapper.getEntity() == null && limitWrapper.getSpentGrantCurrency() == null) {
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


}
