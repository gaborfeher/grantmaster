package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.framework.base.TablePageControllerBase;
import com.github.gaborfeher.grantmaster.framework.base.TabSelectionChangeListener;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.framework.base.EntityWrapper;
import com.github.gaborfeher.grantmaster.framework.base.EditableTableRowItem;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javax.persistence.EntityManager;

public class ProjectTabController extends TablePageControllerBase {
  @FXML TabPane projectTabs;
  @FXML ProjectExpenseTabController projectExpenseTabController;
  @FXML ProjectSourceTabController projectSourceTabController;
  @FXML ProjectBudgetCategoriesTabController projectBudgetCategoriesTabController;
  @FXML ProjectNotesTabController projectNotesTabController;
  @FXML ProjectReportsTabController projectReportsTabController;

  void init(Project project) {
    projectTabs.getSelectionModel().selectedItemProperty().addListener(new TabSelectionChangeListener());
    projectExpenseTabController.init(project);
    projectSourceTabController.init(project);
    projectBudgetCategoriesTabController.init(project);
    projectNotesTabController.init(project);
    projectReportsTabController.init(project);
  }
  
  @Override
  public void onMyTabIsSelected() {
    Tab tab = projectTabs.getSelectionModel().getSelectedItem();
    TabSelectionChangeListener.activateTab(tab);
  }

  @Override
  protected void getItemListForRefresh(EntityManager em, List items) {
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  protected EditableTableRowItem createNewEntity(EntityManager em) {
    throw new UnsupportedOperationException("Not supported.");
  }
  
  @Override
  public void onRefresh() {
    throw new UnsupportedOperationException("Not supported.");
  }
}
