package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectBudgetLimit;
import com.github.gaborfeher.grantmaster.core.RefreshControlSingleton;
import com.github.gaborfeher.grantmaster.core.Utils;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.BudgetCategoryWrapper;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectBudgetCategoryWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectSourceWrapper;
import java.sql.Date;
import java.util.Arrays;
import javafx.scene.control.DatePicker;

public class ProjectBudgetCategoriesTabController extends RefreshControlSingleton.MessageObserver implements Initializable {  
  @FXML TableView<BudgetCategoryWrapper> table;
  @FXML TableColumn<BudgetCategoryWrapper, Double> spentGrantCurrencyColumn;
  @FXML TableColumn<BudgetCategoryWrapper, Double> spentAccountingCurrencyColumn;  
  @FXML TableColumn<BudgetCategoryWrapper, Double> remainingGrantCurrencyColumn;
  @FXML TableColumn<BudgetCategoryWrapper, Double> remainingAccountingCurrencyColumn;
  @FXML TableColumn<BudgetCategoryWrapper, Double> budgetGrantCurrencyColumn;
  @FXML TableColumn<BudgetCategoryWrapper, Double> budgetAccountingCurrencyColumn;
  
  @FXML DatePicker filterStartDate;
  @FXML DatePicker filterEndDate;
  
  /**
   * Project currently being opened.
   */
  Project project;
  
  public void createButtonAction(ActionEvent event) {
    ProjectBudgetLimit limit = new ProjectBudgetLimit();
    limit.setProject(project);
    ProjectBudgetCategoryWrapper wrapper = new ProjectBudgetCategoryWrapper(limit.getBudgetCategory(), 0.0, 0.0);
    wrapper.setProject(project);
    wrapper.setLimit(0.0, limit);
    wrapper.setState(EntityWrapper.State.EDITING_NEW);
    table.getItems().add(wrapper);
  }
  
  public void filterUpdateAction(ActionEvent event) {
    refresh();
  }
  
  public void filterResetButtonAction(ActionEvent event) {
    filterStartDate.setValue(null);
    filterEndDate.setValue(null);
    refresh();
  }
  
  void init(Project project) {
    this.project = project;
    subscribe();
  }
  
  @Override
  public void initialize(URL url, ResourceBundle rb) {
  }  

  @Override
  public void refresh() {
    System.out.println("refresh");
    Date startDate = Utils.toSqlDate(filterStartDate.getValue());
    Date endDate = Utils.toSqlDate(filterEndDate.getValue());
    
    List paymentLines = 
        ProjectBudgetCategoryWrapper.getProjectBudgetLimits(
            project,
            startDate,
            endDate);
    ProjectBudgetCategoryWrapper incomingItem = new ProjectBudgetCategoryWrapper(project.getIncomeType().getName());
    for (ProjectSourceWrapper source : ProjectSourceWrapper.getProjectSources(project, startDate, endDate)) {
      incomingItem.addAmounts(source.getGrantCurrencyAmount(), source.getAccountingCurrencyAmount());
    }
    
    BudgetCategoryWrapper.createBudgetSummaryList(paymentLines,
        Arrays.asList((BudgetCategoryWrapper)incomingItem),
        table.getItems());
    
    spentAccountingCurrencyColumn.setText(project.getAccountCurrency().getCode());
    spentGrantCurrencyColumn.setText(project.getGrantCurrency().getCode());
    remainingAccountingCurrencyColumn.setText(project.getAccountCurrency().getCode());
    remainingGrantCurrencyColumn.setText(project.getGrantCurrency().getCode());
    budgetAccountingCurrencyColumn.setText(project.getAccountCurrency().getCode());
    budgetGrantCurrencyColumn.setText(project.getGrantCurrency().getCode());
  }
}
