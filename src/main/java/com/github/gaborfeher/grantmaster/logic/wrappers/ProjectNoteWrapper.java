package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectNote;
import java.sql.Timestamp;
import java.util.List;

public class ProjectNoteWrapper extends EntityWrapper {
  private ProjectNote note;
  
  public ProjectNoteWrapper(ProjectNote note) {
    this.note = note;
  }
  
  public String getNote() {
    return note.getNote();
  }
  
  public void setNote(String note) {
    this.note.setNote(note);
  }
  
  public Timestamp getEntryTime() {
    return note.getEntryTime();
  }
  
  @Override
  protected Object getEntity() {
    return note;
  }
  
  public static List<ProjectNoteWrapper> getNotes(Project project) {
    return DatabaseConnectionSingleton.getInstance().
        createQuery(
            "SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectNoteWrapper(n) " +
            "FROM ProjectNote n " +
            "WHERE n.project = :project " +
            "ORDER BY n.entryTime",
            ProjectNoteWrapper.class).
        setParameter("project", project).
        getResultList();
  }
  
}
