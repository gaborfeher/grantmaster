/**
 * This file is a part of GrantMaster.
 * Copyright (C) 2015  Gábor Fehér <feherga@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.framework.base.EntityWrapper;
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
