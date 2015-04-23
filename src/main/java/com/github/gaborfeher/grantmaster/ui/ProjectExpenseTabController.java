package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectExpense;
import javafx.fxml.FXML;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectExpenseWrapper;
import java.math.BigDecimal;
import javax.persistence.EntityManager;

public class ProjectExpenseTabController extends ControllerBase<ProjectExpenseWrapper> {
  @FXML ExpenseTableController tableController;
  
  Project project;

  public ProjectExpenseTabController() {
  }
  
  void init(Project project) {
    this.project = project;
    tableController.init(project);
  }
  
  @Override
  public void refresh(EntityManager em) {
    table.getItems().setAll(ProjectExpenseWrapper.getProjectExpenseList(em, project));
  }

  @Override
  public ProjectExpenseWrapper createNewEntity() {
    return ProjectExpenseWrapper.createNew(project);
  }

}
