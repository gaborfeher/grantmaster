package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.ui.framework.TablePageControllerBase;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectNote;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectNoteWrapper;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;

public class ProjectNotesTabController extends TablePageControllerBase<ProjectNoteWrapper> {
  
  Project project;
  
  void init(Project project) {
    this.project = project;
  }

  @Override
  public void getItemListForRefresh(EntityManager em, List<ProjectNoteWrapper> items) {
    items.addAll(ProjectNoteWrapper.getNotes(em, project));
  }

  @Override
  protected ProjectNoteWrapper createNewEntity(EntityManager em) {
    ProjectNote projectNote = new ProjectNote();
    projectNote.setEntryTime(new Timestamp(new Date().getTime()));
    projectNote.setProject(project);
    return new ProjectNoteWrapper(projectNote);
  }

}
