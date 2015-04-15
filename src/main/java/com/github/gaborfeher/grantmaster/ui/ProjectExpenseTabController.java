package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectExpense;
import com.github.gaborfeher.grantmaster.core.RefreshControlSingleton;
import com.github.gaborfeher.grantmaster.core.RefreshMessage;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectExpenseWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ProjectExpenseTabController extends RefreshControlSingleton.MessageObserver implements Initializable {
  @FXML TableView<ProjectExpenseWrapper> projectExpensesTable;
  
  @FXML TableColumn<ProjectExpense, Float> projectExpensesAccountingCurrencyAmountColumn;
  @FXML TableColumn<ProjectExpense, Float> projectExpensesGrantCurrencyAmountColumn;
    
  Project project;
  
  ObservableList<ProjectExpenseWrapper> list = FXCollections.observableArrayList();

  public ProjectExpenseTabController() {
  }
  
  void init(Project project) {
    this.project = project;
    projectExpensesTable.setItems(list);
    RefreshControlSingleton.getInstance().subscribe(this);
  }
  
  @Override
  public void refresh(RefreshMessage message) {
    System.out.println("refresh " + this);
    
    List<ProjectExpenseWrapper> projectExpenses = ProjectExpenseWrapper.getProjectExpenseList(project);
    list.setAll(projectExpenses);
    
    projectExpensesAccountingCurrencyAmountColumn.setText(project.getAccountCurrency().toString());
    projectExpensesGrantCurrencyAmountColumn.setText(project.getGrantCurrency().toString());    
  }
  
  @Override
  public boolean forMe(RefreshMessage message) {
    return message.getSourceProject() == project;
  }
  
  public void createProjectExpenseButtonAction(ActionEvent event) {
    ProjectExpense expense = new ProjectExpense();
    expense.setProject(project);
    ProjectExpenseWrapper wrapper = new ProjectExpenseWrapper(expense, 0.0, 0.0);
    wrapper.setState(EntityWrapper.State.EDITING_NEW);
    list.add(wrapper);
  }

  @Override
  public void initialize(URL url, ResourceBundle rb) {
  }

}
