package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectWrapper;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;

public class ProjectListTabController extends ControllerBase<ProjectWrapper> {
  MainPageController parent;
  
  @Override
  public void refresh() {
    table.getItems().setAll(ProjectWrapper.getProjects(this));
  }
  
  public void handleOpenButtonAction(ActionEvent event) throws IOException {
    int selectedIndex = table.getSelectionModel().getSelectedIndex();
    if (selectedIndex < 0) {
      return;
    }
    ProjectWrapper selectedProjectWrapper = table.getItems().get(selectedIndex);
    if (selectedProjectWrapper.getState() != EntityWrapper.State.SAVED) {
      return;
    }
    parent.addProjectTab(selectedProjectWrapper.getProject());
  }
  
  void init(MainPageController parent) {
    this.parent = parent;
  }

  @Override
  protected ProjectWrapper createNewEntity() {
    Project newProject = new Project();
    return new ProjectWrapper(newProject);
  }
}
