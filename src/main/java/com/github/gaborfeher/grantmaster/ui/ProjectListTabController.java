package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.core.RefreshControlSingleton;
import com.github.gaborfeher.grantmaster.core.Utils;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectWrapper;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;

public class ProjectListTabController extends RefreshControlSingleton.MessageObserver implements Initializable {

  @FXML TableView<ProjectWrapper> table;
  
  MainPageController parent;

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    subscribe();
  }  
  
  @Override
  public void refresh() {
    table.getItems().setAll(ProjectWrapper.getProjects());
  }
  
  public void handleAddButtonAction(ActionEvent event) {
    Project newProject = new Project();
    ProjectWrapper projectWrapper = new ProjectWrapper(newProject);
    Utils.addNewEntityForEditing(projectWrapper, table.getItems());
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
        
    RefreshControlSingleton.getInstance().broadcastRefresh();
  }
  
  void init(MainPageController parent) {
    this.parent = parent;
  }
}
