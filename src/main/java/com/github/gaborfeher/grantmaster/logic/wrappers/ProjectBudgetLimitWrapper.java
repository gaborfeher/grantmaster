package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.ExpenseType;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectBudgetLimit;
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 *
 * @author gabor
 */
public class ProjectBudgetLimitWrapper extends EntityWrapper {
  final private ProjectBudgetLimit limit;
  final private double spent;
  final private SimpleDoubleProperty remaining;
  
  public ProjectBudgetLimitWrapper(ProjectBudgetLimit limit, double spent) {
    this.limit = limit;
    this.spent = spent;
    this.remaining = new SimpleDoubleProperty(limit.getBudget() - spent);
  }
  
  public double getBudget() {
    return limit.getBudget();
  }
  
  public void setBudget(Double budget) {
    limit.setBudget(budget);
    this.remaining.set(limit.getBudget() - spent);

  }
  
  public double getSpent() {
    return spent;
  }
  
  public DoubleProperty remainingProperty() {
    return remaining;
  }
  
  public ExpenseType getExpenseType() {
    return limit.getExpenseType();
  }
  
  public void setExpenseType(ExpenseType expenseType) {
    limit.setExpenseType(expenseType);
  }
  
  public static List<ProjectBudgetLimitWrapper> getProjectBudgetLimits(Project project) {
    EntityManager em = DatabaseConnectionSingleton.getInstance().em();
    TypedQuery<ProjectBudgetLimitWrapper> query = em.createQuery(
        "SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectBudgetLimitWrapper(l, COALESCE(SUM(a.accountingCurrencyAmount / s.exchangeRate), 0.0)) " +
        "FROM ProjectBudgetLimit l LEFT OUTER JOIN l.expenseType et LEFT OUTER JOIN ProjectExpense e ON e.expenseType = et LEFT OUTER JOIN ExpenseSourceAllocation a ON a.expense = e LEFT OUTER JOIN ProjectSource s ON a.source = s AND s.project = :project WHERE l.project = :project GROUP BY l",
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

}
