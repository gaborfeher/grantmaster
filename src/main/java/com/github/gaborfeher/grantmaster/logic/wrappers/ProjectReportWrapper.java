package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectReport;
import java.util.List;
import javax.persistence.EntityManager;

public class ProjectReportWrapper extends EntityWrapper<ProjectReport> {

  public static EntityWrapper createNew(Project project) {
    ProjectReport report = new ProjectReport();
    report.setProject(project);
    return new ProjectReportWrapper(report);
  }

  static void removeReports(EntityManager em, Project project) {
    em.createQuery("DELETE FROM ProjectReport r WHERE r.project = :project").setParameter("project", project).executeUpdate();
  }

  public ProjectReportWrapper(ProjectReport entity) {
    super(entity);
  }
  
  public static List<ProjectReportWrapper> getProjectReports(
      EntityManager em, Project project) {
    return em.createQuery(
        "SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectReportWrapper(r) " +
        "FROM ProjectReport r " +
        "WHERE r.project = :project " +
        "ORDER BY r.reportDate DESC",
        ProjectReportWrapper.class).
        setParameter("project", project).
        getResultList();
  }
  
  public static List<ProjectReport> getProjectReportsWithoutWrapping(
      EntityManager em, Project project) {
    return em.createQuery(
        "SELECT r " +
        "FROM ProjectReport r " +
        "WHERE r.project = :project " +
        "ORDER BY r.reportDate DESC",
          ProjectReport.class).
          setParameter("project", project).
          getResultList();
  }
  
  public static ProjectReport getDefaultProjectReport(EntityManager em, Project project) {
    List<ProjectReport> reports = getProjectReportsWithoutWrapping(em, project);
    if (!reports.isEmpty()) {
      return reports.get(0);
    } else {
      return null;
    }
  }

}
