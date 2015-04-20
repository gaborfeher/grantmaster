package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.core.RefreshControlSingleton;
import com.github.gaborfeher.grantmaster.core.TransactionRunner;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import java.util.List;
import javax.persistence.EntityManager;

public class ProjectWrapper extends EntityWrapper {
  Project project;

  public ProjectWrapper(Project project) {
    this.project = project;
  }
  
  public int getId() {
    return project.getId();
  }
  
  public String getName() {
    return project.getName();
  }
  
  public void setName(String name) {
    project.setName(name);
  }
  
  public Currency getAccountCurrency() {
    return project.getAccountCurrency();
  }
  
  public void setAccountCurrency(Currency currency) {
    project.setAccountCurrency(currency);
  }
  
  public Currency getGrantCurrency() {
    return project.getGrantCurrency();
  }
  
  public void setGrantCurrency(Currency currency) {
    project.setGrantCurrency(currency);
  }
  
  public void setIncomeType(BudgetCategory budgetCategory) {
    project.setIncomeType(budgetCategory);
  }

  public BudgetCategory getIncomeType() {
    return project.getIncomeType();
  }
  
  @Override
  protected Object getEntity() {
    return project;
  }
  
  @Override
  public void delete() {
    DatabaseConnectionSingleton.getInstance().runInTransaction(new TransactionRunner() {
      @Override
      public boolean run(EntityManager em) {
        ProjectExpenseWrapper.removeProjectExpenses(em, project);
        ProjectSourceWrapper.removeProjectSources(em, project);
        ProjectBudgetCategoryWrapper.removeProjectBudgetLimits(em, project);
        em.remove(project);
        return true;
      }
      @Override
      public void onFailure() {
        DatabaseConnectionSingleton.getInstance().hardReset();
      }
      @Override
      public void onSuccess() {
        RefreshControlSingleton.getInstance().broadcastRefresh();
      }
    });
  }

  public Project getProject() {
    return project;
  }
  
  public static List<ProjectWrapper> getProjects() {
    return DatabaseConnectionSingleton.getInstance().createQuery("SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectWrapper(p) FROM Project p", ProjectWrapper.class).getResultList();
  }
  
  public static List<Project> getProjectsWithoutWrapping() {
    return DatabaseConnectionSingleton.getInstance().createQuery("SELECT p FROM Project p", Project.class).getResultList();
  }
  
 
}
