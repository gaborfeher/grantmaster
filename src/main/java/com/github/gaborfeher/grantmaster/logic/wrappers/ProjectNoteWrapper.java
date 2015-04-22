package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.EntityBase;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectNote;
import com.github.gaborfeher.grantmaster.ui.ControllerBase;
import java.util.List;
import javax.persistence.EntityManager;

public class ProjectNoteWrapper extends EntityWrapper {
  private ProjectNote note;
  
  public ProjectNoteWrapper(ProjectNote note) {
    this.note = note;
  }
  
  @Override
  protected EntityBase getEntity() {
    return note;
  }
  
  @Override
  protected void setEntity(EntityBase entity) {
    this.note = (ProjectNote) entity;
  }
  
  public static List<ProjectNoteWrapper> getNotes(EntityManager em, Project project) {
    return em.createQuery(
            "SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectNoteWrapper(n) " +
            "FROM ProjectNote n " +
            "WHERE n.project = :project " +
            "ORDER BY n.entryTime",
            ProjectNoteWrapper.class).
        setParameter("project", project).
        getResultList();
  }
  
}
