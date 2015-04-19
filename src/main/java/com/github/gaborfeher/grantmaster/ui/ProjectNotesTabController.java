package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectNote;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectNoteWrapper;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;

public class ProjectNotesTabController implements Initializable {
  @FXML TableView<ProjectNoteWrapper> table;
  
  Project project;
  
  void init(Project project) {
    this.project = project;
    refresh();
  }
  
  @Override
  public void initialize(URL url, ResourceBundle rb) {
  }  
  
  public void newNoteButton() {
    ProjectNote projectNote = new ProjectNote();
    projectNote.setEntryTime(new Timestamp(new Date().getTime()));
    projectNote.setProject(project);
    ProjectNoteWrapper wrapper = new ProjectNoteWrapper(projectNote);
    wrapper.setState(EntityWrapper.State.EDITING_NEW);
    table.getItems().add(wrapper);
  }
  
  void refresh() {
    table.getItems().setAll(ProjectNoteWrapper.getNotes(project));
  }

}
