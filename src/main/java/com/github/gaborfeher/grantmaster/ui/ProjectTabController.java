package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
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
  public void refresh(EntityManager em) {
    this.projectExpenseTabController.refresh();
    this.projectSourceTabController.refresh();
    this.projectBudgetCategoriesTabController.refresh();
    this.projectNotesTabController.refresh();
  }

}
