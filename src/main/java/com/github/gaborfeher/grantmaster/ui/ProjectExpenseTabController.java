package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectExpense;
import com.github.gaborfeher.grantmaster.core.RefreshControlSingleton;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectExpenseWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ProjectExpenseTabController extends RefreshControlSingleton.MessageObserver implements Initializable {
  @FXML TableView<ProjectExpenseWrapper> table;
  @FXML ExpenseTableController tableController;
  
  Project project;
  
  ObservableList<ProjectExpenseWrapper> list = FXCollections.observableArrayList();

  public ProjectExpenseTabController() {
  }
  
  void init(Project project) {
    this.project = project;
    tableController.init(project);
    table.setItems(list);
    subscribe();
  }
  
  @Override
  public void refresh() {
    List<ProjectExpenseWrapper> projectExpenses = ProjectExpenseWrapper.getProjectExpenseList(project);
    list.setAll(projectExpenses);
  }

  public void createProjectExpenseButtonAction(ActionEvent event) {
    ProjectExpense expense = new ProjectExpense();
    expense.setProject(project);
    ProjectExpenseWrapper wrapper = new ProjectExpenseWrapper(expense, 0.0, 0.0);
    wrapper.setState(EntityWrapper.State.EDITING_NEW);
    wrapper.setOriginalCurrency(project.getAccountCurrency());
    list.add(wrapper);
  }

  @Override
  public void initialize(URL url, ResourceBundle rb) {
  }

}
