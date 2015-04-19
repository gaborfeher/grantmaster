package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.core.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.ExpenseType;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectBudgetLimit;
import java.sql.Date;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;

/**
 *
 * @author gabor
 */
public class ProjectBudgetLimitWrapper extends EntityWrapper {
  private ProjectBudgetLimit limit;
  final private Double spentGrantCurrency;
  final private Double spentAccountingCurrency;
  private Double remainingGrantCurrency;
  private ExpenseType expenseType;
  private Project project;
  
  public ProjectBudgetLimitWrapper(ExpenseType expenseType, Double spentAccountingCurrency, Double spentGrantCurrency) {
    this.spentGrantCurrency = spentGrantCurrency;
    this.spentAccountingCurrency = spentAccountingCurrency;
    this.expenseType = expenseType;
    this.remainingGrantCurrency = null;
  }
  
  public void setProject(Project project) {
    this.project = project;
  }
  
  public void setLimit(double total, ProjectBudgetLimit limit) {
    this.limit = limit;
    if (limit == null) {
      return;
    }
    
    if (expenseType != null && expenseType.getId() != limit.getExpenseType().getId()) {
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
  
  public Double getBudgetGrantCurrency() {
    if (limit == null) {
      return null;
    }
    return limit.getBudget();
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
  
  public ExpenseType getExpenseType() {
    return expenseType;
  }

  public void setExpenseType(ExpenseType expenseType) {
    limit.setExpenseType(expenseType);
    this.expenseType = expenseType;
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
  protected Object getEntity() {
    return limit;
  }
  
  @Override
  public void setState(State state) {
    if (state == State.EDITING) {
      if (limit == null) {
        state = State.EDITING_NEW;
        limit = new ProjectBudgetLimit();
        limit.setExpenseType(expenseType);
        limit.setProject(project);
      }
    }
    super.setState(state);
  }

  /**
   * @return the spentAccountingCurrency
   */
  public Double getSpentAccountingCurrency() {
    return spentAccountingCurrency;
  }
  
  
  public static List<ProjectBudgetLimitWrapper> getProjectBudgetLimits(
      Project project,
      Date filterStartDate,
      Date filterEndDate) {
    System.out.println("getLimits " + filterStartDate + " " + filterEndDate);
    
    EntityManager em = DatabaseConnectionSingleton.getInstance().em();
    Double total = 
        Utils.getSingleResultWithDefault(0.0,
            em.createQuery("SELECT SUM(s.amount) FROM ProjectSource s WHERE s.project = :project GROUP BY s.project", Double.class).
            setParameter("project", project));
        
    List<ProjectBudgetLimitWrapper> list = em.createQuery(
        "SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectBudgetLimitWrapper(et, SUM(a.accountingCurrencyAmount), SUM(a.accountingCurrencyAmount / s.exchangeRate)) " +
        "FROM ExpenseType et LEFT OUTER JOIN ProjectExpense e ON e.expenseType = et AND e.project = :project LEFT OUTER JOIN ExpenseSourceAllocation a ON a.expense = e LEFT OUTER JOIN ProjectSource s ON a.source = s AND s.project = :project " +
            "WHERE et.direction = :direction " +
            " AND (:filterStartDate IS NULL OR e.paymentDate >= :filterStartDate) " +
            " AND (:filterEndDate IS NULL OR e.paymentDate <= :filterEndDate) " +
            "GROUP BY et " +
            "ORDER BY et.groupName NULLS LAST, et.name",
        ProjectBudgetLimitWrapper.class).
            setParameter("project", project).
            setParameter("direction", ExpenseType.Direction.PAYMENT).
            setParameter("filterStartDate", filterStartDate).
            setParameter("filterEndDate", filterEndDate).
            getResultList();
    
    Iterator<ProjectBudgetLimitWrapper> iterator = list.iterator();
    while (iterator.hasNext()) {
      ProjectBudgetLimitWrapper limitWrapper = iterator.next();
      System.out.println("checking: " + limitWrapper.getExpenseType().getName());
      
      limitWrapper.setProject(project);
      ProjectBudgetLimit limit = Utils.getSingleResultWithDefault(null, em.createQuery(
          "SELECT l FROM ProjectBudgetLimit l WHERE l.project = :project AND l.expenseType = :expenseType",
          ProjectBudgetLimit.class).
          setParameter("project", project).
          setParameter("expenseType", limitWrapper.getExpenseType()));

      limitWrapper.setLimit(total, limit);
      if (limitWrapper.getEntity() == null && limitWrapper.getSpentGrantCurrency() == null) {
        System.out.println("DISCARDED");
        iterator.remove();
      }
    }

    return list;
  }
  
  static void removeProjectBudgetLimits(Project project) {
    EntityManager em = DatabaseConnectionSingleton.getInstance().em();
    em.createQuery("DELETE FROM ProjectBudgetLimit l WHERE l.project = :project").
        setParameter("project", project).
        executeUpdate();
  }

  public String getGroupName() {
    return getExpenseType().getGroupName();
  }
  
}
