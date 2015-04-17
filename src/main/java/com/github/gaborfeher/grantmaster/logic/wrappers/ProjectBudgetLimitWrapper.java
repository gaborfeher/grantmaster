package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.ExpenseType;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectBudgetLimit;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 *
 * @author gabor
 */
public class ProjectBudgetLimitWrapper extends EntityWrapper {
  private ProjectBudgetLimit limit;
  final private Double spent;
  private Double remaining;
  private ExpenseType expenseType;
  private Project project;
  
  public ProjectBudgetLimitWrapper(ExpenseType expenseType, Double spent) {
    this.spent = spent;
    this.expenseType = expenseType;
    this.remaining = null;
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
      this.remaining = limit.getBudget();
      if (spent != null) {
        this.remaining -= spent;
      }
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
    remaining = limit.getBudget();
    if (spent != null) {
      remaining -= spent;
    }
  }
  
  public Double getSpent() {
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
        "SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectBudgetLimitWrapper(et, SUM(a.accountingCurrencyAmount / s.exchangeRate)) " +
        "FROM ExpenseType et LEFT OUTER JOIN ProjectExpense e ON e.expenseType = et LEFT OUTER JOIN ExpenseSourceAllocation a ON a.expense = e LEFT OUTER JOIN ProjectSource s ON a.source = s AND s.project = :project  " +
            "GROUP BY et " +
            "ORDER BY et.name",
        ProjectBudgetLimitWrapper.class);    
    query.setParameter("project", project);
    List<ProjectBudgetLimitWrapper> list = query.getResultList();
    
    Iterator<ProjectBudgetLimitWrapper> iterator = list.iterator();
    while (iterator.hasNext()) {
      ProjectBudgetLimitWrapper limitWrapper = iterator.next();
      limitWrapper.setProject(project);
      ProjectBudgetLimit limit = null;
      List<ProjectBudgetLimit> limits = em.createQuery(
          "SELECT l FROM ProjectBudgetLimit l WHERE l.project = :project AND l.expenseType = :expenseType",
          ProjectBudgetLimit.class).
          setParameter("project", project).
          setParameter("expenseType", limitWrapper.getExpenseType()).
          getResultList();
      if (limits.size() >= 1) {
        // TODO
        if (limits.size() > 1) {
          System.out.println("strange limit size");
        }
        limit = limits.get(0);
      }
      limitWrapper.setLimit(total, limit);
      if (limitWrapper.getEntity() == null && limitWrapper.getSpent() == null) {
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
