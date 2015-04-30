package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.ui.framework.TablePageControllerBase;
import javafx.fxml.FXML;
import java.util.List;
import javax.persistence.EntityManager;

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectExpenseWrapper;

public class ProjectExpenseTabController extends TablePageControllerBase<ProjectExpenseWrapper> {
  @FXML ExpenseTableController tableController;
  
  Project project;

  public ProjectExpenseTabController() {
  }
  
  void init(Project project) {
    this.project = project;
    tableController.init(project);
  }
  
  @Override
  public void getItemListForRefresh(EntityManager em, List<ProjectExpenseWrapper> items) {
    items.addAll(ProjectExpenseWrapper.getProjectExpenseList(em, project));
    tableController.refresh();
  }

  @Override
  public ProjectExpenseWrapper createNewEntity(EntityManager em) {
    return ProjectExpenseWrapper.createNew(em, project);
  }

}
