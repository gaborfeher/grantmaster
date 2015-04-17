package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.ExpenseType;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectBudgetLimit;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 *
 * @author gabor
 */
public class ProjectBudgetLimitWrapper extends EntityWrapper {
  private ProjectBudgetLimit limit;
  final private double spent;
  private Double remaining;
  private ExpenseType expenseType;
  private Project project;
  
  public ProjectBudgetLimitWrapper(Project project, ExpenseType expenseType, ProjectBudgetLimit limit, double spent, double total) {
    this.limit = limit;
    this.spent = spent;
    this.expenseType = expenseType;
    this.remaining = null;
    this.project = project;
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
      limit.setBudget((Double)total * limit.getBudgetPercentage() / 100.0);
    }
    if (limit.getBudget() != null) {
      this.remaining = limit.getBudget() - spent;
    }
  }
  
  public Double getBudget() {
    if (limit == null) {
      return null;
    }
    return limit.getBudget();
  }
  
  public void setBudget(Double budget) {
    limit.setBudget(budget);
    remaining = limit.getBudget() - spent;
  }
  
  public double getSpent() {
    return spent;
  }
  
  public Double getRemaining() {
    return remaining;
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
  
  public static List<ProjectBudgetLimitWrapper> getProjectBudgetLimits(Project project) {
    EntityManager em = DatabaseConnectionSingleton.getInstance().em();
    Double total = em.createQuery("SELECT SUM(s.amount) FROM ProjectSource s WHERE s.project = :project GROUP BY s.project", Double.class).
        setParameter("project", project).
        getSingleResult();
    
//    TypedQuery<ProjectBudgetLimitWrapper> query = em.createQuery(
//        "SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectBudgetLimitWrapper(l, COALESCE(SUM(a.accountingCurrencyAmount / s.exchangeRate), 0.0), " + total + ") " +
//        "FROM ProjectBudgetLimit l LEFT OUTER JOIN l.expenseType et LEFT OUTER JOIN ProjectExpense e ON e.expenseType = et LEFT OUTER JOIN ExpenseSourceAllocation a ON a.expense = e LEFT OUTER JOIN ProjectSource s ON a.source = s AND s.project = :project " +
//            "WHERE l.project = :project " +
//            "GROUP BY l " +
//            "ORDER BY et.name",
//        ProjectBudgetLimitWrapper.class);
    
    
    TypedQuery<ProjectBudgetLimitWrapper> query = em.createQuery(
        "SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectBudgetLimitWrapper(s.project, et, l, COALESCE(SUM(a.accountingCurrencyAmount / s.exchangeRate), 0.0), " + total + ") " +
        "FROM ExpenseType et INNER JOIN ProjectExpense e ON e.expenseType = et INNER JOIN ExpenseSourceAllocation a ON a.expense = e INNER JOIN ProjectSource s ON a.source = s LEFT OUTER JOIN ProjectBudgetLimit l ON l.expenseType = et AND l.project = :project " +
            "WHERE s.project = :project " +
            "GROUP BY et " +
            "ORDER BY et.name",
        ProjectBudgetLimitWrapper.class);    
    
    
    query.setParameter("project", project);
    return query.getResultList();
  }
  
  static void removeProjectBudgetLimits(Project project) {
    EntityManager em = DatabaseConnectionSingleton.getInstance().em();
    em.createQuery("DELETE FROM ProjectBudgetLimit l WHERE l.project = :project").
        setParameter("project", project).
        executeUpdate();
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
  
}
