package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectNote;
import java.util.List;
import javax.persistence.EntityManager;

public class ProjectNoteWrapper extends EntityWrapper {
  public ProjectNoteWrapper(ProjectNote note) {
    super(note);
  }
  
  public static List<ProjectNoteWrapper> getNotes(EntityManager em, Project project) {
    return em.createQuery(
            "SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectNoteWrapper(n) " +
            "FROM ProjectNote n " +
            "WHERE n.project = :project " +
            "ORDER BY n.entryTime DESC",
            ProjectNoteWrapper.class).
        setParameter("project", project).
        getResultList();
  }
  
  static void removeProjectNotes(EntityManager em, Project project) {
    em.createQuery("DELETE FROM ProjectNote n WHERE n.project = :project").
        setParameter("project", project).
        executeUpdate();
  }
}
