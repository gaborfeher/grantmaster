package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.core.TransactionRunner;
import com.github.gaborfeher.grantmaster.logic.entities.EntityBase;
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
  
  @Override
  protected EntityBase getEntity() {
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
      }
      @Override
      public void onSuccess() {
        getParent().refresh();
      }
    });
  }

  public Project getProject() {
    return project;
  }
  
  public static List<ProjectWrapper> getProjects(EntityManager em) {
    return em.createQuery(
        "SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectWrapper(p) FROM Project p",
        ProjectWrapper.class).
            getResultList();
  }
  
  public static List<Project> getProjectsWithoutWrapping(EntityManager em) {
    return em.createQuery("SELECT p FROM Project p", Project.class).getResultList();
  }
  
  public static ProjectWrapper createNew() {
    Project newProject = new Project();
    return new ProjectWrapper(newProject);
  }

  @Override
  protected void setEntity(EntityBase entity) {
    this.project = (Project) entity;
  }

}
