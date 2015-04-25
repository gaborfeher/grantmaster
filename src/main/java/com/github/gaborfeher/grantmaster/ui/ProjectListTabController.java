package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectWrapper;
import com.github.gaborfeher.grantmaster.ui.cells.EditButtonTableCell;
import java.io.IOException;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javax.persistence.EntityManager;

public class ProjectListTabController extends ControllerBase<ProjectWrapper> {
  MainPageController parent;
  
  @Override
  public void getItemListForRefresh(EntityManager em, List<ProjectWrapper> items) {
    items.addAll(ProjectWrapper.getProjects(em));
  }
  
  public void handleOpenButtonAction(ActionEvent event) throws IOException {
    Node sourceButton = (Node) event.getSource();
    EditButtonTableCell sourceCell = (EditButtonTableCell) sourceButton.getProperties().get("tableCell");
    ProjectWrapper selectedProjectWrapper = (ProjectWrapper) sourceCell.getEntityWrapper();

  /*  int selectedIndex = table.getSelectionModel().getSelectedIndex();
    if (selectedIndex < 0) {
      return;
    }
    ProjectWrapper selectedProjectWrapper = table.getItems().get(selectedIndex);*/
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
    return ProjectWrapper.createNew();
  }
}
