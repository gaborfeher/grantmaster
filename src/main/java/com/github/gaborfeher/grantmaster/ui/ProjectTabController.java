package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import java.util.List;
import javafx.fxml.FXML;
import javax.persistence.EntityManager;

public class ProjectTabController extends ControllerBase {
  @FXML ProjectExpenseTabController projectExpenseTabController;
  @FXML ProjectSourceTabController projectSourceTabController;
  @FXML ProjectBudgetCategoriesTabController projectBudgetCategoriesTabController;
  @FXML ProjectNotesTabController projectNotesTabController;
  @FXML ProjectReportsTabController projectReportsTabController;

  void init(Project project) {   
    projectExpenseTabController.init(project);
    projectSourceTabController.init(project);
    projectBudgetCategoriesTabController.init(project);
    projectNotesTabController.init(project);
    projectReportsTabController.init(project);
  }

  @Override
  public void refreshContent() {
    projectExpenseTabController.onRefresh();
    projectSourceTabController.onRefresh();
    projectBudgetCategoriesTabController.onRefresh();
    projectNotesTabController.onRefresh();
    projectReportsTabController.onRefresh();
  }

  @Override
  protected void getItemListForRefresh(EntityManager em, List items) {
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  protected EntityWrapper createNewEntity(EntityManager em) {
    throw new UnsupportedOperationException("Not supported.");
  }
}
