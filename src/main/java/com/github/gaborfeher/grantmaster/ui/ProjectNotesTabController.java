package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectNote;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectNoteWrapper;
import java.sql.Timestamp;
import java.util.Date;
import javax.persistence.EntityManager;

public class ProjectNotesTabController extends ControllerBase<ProjectNoteWrapper> {
  
  Project project;
  
  void init(Project project) {
    this.project = project;
  }

  @Override
  public void refresh(EntityManager em) {
    table.getItems().setAll(ProjectNoteWrapper.getNotes(em, project));
  }

  @Override
  protected ProjectNoteWrapper createNewEntity() {
    ProjectNote projectNote = new ProjectNote();
    projectNote.setEntryTime(new Timestamp(new Date().getTime()));
    projectNote.setProject(project);
    return new ProjectNoteWrapper(projectNote);
  }

}
