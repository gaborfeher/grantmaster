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

  void init(Project project) {   
    this.projectExpenseTabController.init(project);
    this.projectSourceTabController.init(project);
    this.projectBudgetCategoriesTabController.init(project);
    this.projectNotesTabController.init(project);
  }

  @Override
  protected EntityWrapper createNewEntity() {
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  public void getItemListForRefresh(EntityManager em, List items) {
    this.projectExpenseTabController.onRefresh();
    this.projectSourceTabController.onRefresh();
    this.projectBudgetCategoriesTabController.onRefresh();
    this.projectNotesTabController.onRefresh();
  }

}
