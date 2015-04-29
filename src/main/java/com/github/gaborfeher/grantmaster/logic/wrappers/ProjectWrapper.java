package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.ValidatorFactorySingleton;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectReport;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

public class ProjectWrapper extends EntityWrapper<Project> {
  public ProjectWrapper(Project project) {
    super(project);
  }
  
  @Override
  public void delete(EntityManager em) {
    entity = em.find(Project.class, entity.getId());
    ProjectExpenseWrapper.removeProjectExpenses(em, entity);
    ProjectSourceWrapper.removeProjectSources(em, entity);
    ProjectBudgetCategoryWrapper.removeProjectBudgetLimits(em, entity);
    ProjectNoteWrapper.removeProjectNotes(em, entity);
    ProjectReportWrapper.removeReports(em, entity);
    em.remove(entity);
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
    ProjectReport projectReport = new ProjectReport();
    projectReport.setProject(newProject);
    projectReport.setReportDate(LocalDate.now());
    projectReport.setNote("Alapértelmezett report.");
    newProject.setReports(Arrays.asList(projectReport));
    return new ProjectWrapper(newProject);
  }
}
