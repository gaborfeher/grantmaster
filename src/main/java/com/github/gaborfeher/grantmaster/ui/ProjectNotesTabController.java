package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.core.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectNote;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectNoteWrapper;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

public class ProjectNotesTabController
    extends ControllerBase<ProjectNoteWrapper>
    implements Initializable {
  
  Project project;
  
  void init(Project project) {
    this.project = project;
    subscribe();
  }
  
  @Override
  public void initialize(URL url, ResourceBundle rb) {
  }  

  @Override
  public void refresh() {
    table.getItems().setAll(ProjectNoteWrapper.getNotes(project));
  }

  @Override
  protected ProjectNoteWrapper createNewEntity() {
    ProjectNote projectNote = new ProjectNote();
    projectNote.setEntryTime(new Timestamp(new Date().getTime()));
    projectNote.setProject(project);
    return new ProjectNoteWrapper(projectNote);
  }

}
