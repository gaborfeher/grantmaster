package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.framework.base.TablePageControllerBase;
import com.github.gaborfeher.grantmaster.framework.base.EntityWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectWrapper;
import com.github.gaborfeher.grantmaster.framework.base.RowEditState;
import com.github.gaborfeher.grantmaster.framework.ui.cells.EditButtonTableCell;
import java.io.IOException;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javax.persistence.EntityManager;

public class ProjectListTabController extends TablePageControllerBase<ProjectWrapper> {
  MainPageController parent;
  
  @Override
  public void getItemListForRefresh(EntityManager em, List<ProjectWrapper> items) {
    items.addAll(ProjectWrapper.getProjects(em));
  }
  
  public void handleOpenButtonAction(ActionEvent event) throws IOException {
    Node sourceButton = (Node) event.getSource();
    EditButtonTableCell sourceCell = (EditButtonTableCell) sourceButton.getProperties().get("tableCell");
    ProjectWrapper sourceProjectWrapper = (ProjectWrapper) sourceCell.getEntityWrapper();
    if (sourceProjectWrapper.getState() != RowEditState.SAVED) {
      return;
    }
    parent.addProjectTab(sourceProjectWrapper.getEntity());
  }
  
  void init(MainPageController parent) {
    this.parent = parent;
  }

  @Override
  protected ProjectWrapper createNewEntity(EntityManager em) {
    return ProjectWrapper.createNew();
  }
}