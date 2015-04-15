package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.core.RefreshControlSingleton;
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
  
  
  @Override
  protected Object getEntity() {
    return project;
  }
  
  @Override
  public void delete() {
    EntityManager em = DatabaseConnectionSingleton.getInstance().em();
    em.getTransaction().begin();
    ProjectExpenseWrapper.removeProjectExpenses(project);
    ProjectSourceWrapper.removeProjectSources(project);
    ProjectBudgetLimitWrapper.removeProjectBudgetLimits(project);
    em.remove(project);
    em.getTransaction().commit();
    RefreshControlSingleton.getInstance().broadcastRefresh(null);
  }

  public Project getProject() {
    return project;
  }
  
  public static List<ProjectWrapper> getProjects() {
    return DatabaseConnectionSingleton.getInstance().em().createQuery("SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectWrapper(p) FROM Project p", ProjectWrapper.class).getResultList();
  }
}
