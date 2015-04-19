package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectBudgetLimit;
import com.github.gaborfeher.grantmaster.core.RefreshControlSingleton;
import com.github.gaborfeher.grantmaster.core.RefreshMessage;
import com.github.gaborfeher.grantmaster.core.Utils;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.ExpenseTypeWrapper;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectBudgetLimitWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectSourceWrapper;
import java.sql.Date;
import java.util.Arrays;
import javafx.scene.control.DatePicker;

public class ProjectBudgetLimitsTabController extends RefreshControlSingleton.MessageObserver implements Initializable {  
  @FXML TableView<ExpenseTypeWrapper> table;
  @FXML TableColumn<ExpenseTypeWrapper, Double> spentGrantCurrencyColumn;
  @FXML TableColumn<ExpenseTypeWrapper, Double> spentAccountingCurrencyColumn;  
  @FXML TableColumn<ExpenseTypeWrapper, Double> remainingGrantCurrencyColumn;
  @FXML TableColumn<ExpenseTypeWrapper, Double> remainingAccountingCurrencyColumn;
  @FXML TableColumn<ExpenseTypeWrapper, Double> budgetGrantCurrencyColumn;
  @FXML TableColumn<ExpenseTypeWrapper, Double> budgetAccountingCurrencyColumn;
  
  @FXML DatePicker filterStartDate;
  @FXML DatePicker filterEndDate;
  
  Project project;
  ResourceBundle resourceBundle;
  
  public void createButtonAction(ActionEvent event) {
    ProjectBudgetLimit limit = new ProjectBudgetLimit();
    limit.setProject(project);
    ProjectBudgetLimitWrapper wrapper = new ProjectBudgetLimitWrapper(limit.getExpenseType(), 0.0, 0.0);
    wrapper.setProject(project);
    wrapper.setLimit(0.0, limit);
    wrapper.setState(EntityWrapper.State.EDITING_NEW);
    table.getItems().add(wrapper);
  }
  
  public void filterUpdateAction(ActionEvent event) {
    refresh(null);
  }
  
  public void filterResetButtonAction(ActionEvent event) {
    filterStartDate.setValue(null);
    filterEndDate.setValue(null);
    refresh(null);
  }
  
  void init(Project project) {
    this.project = project;
  }
  
  @Override
  public void initialize(URL url, ResourceBundle rb) {
    RefreshControlSingleton.getInstance().subscribe(this);
    this.resourceBundle = rb;
  }  

  @Override
  public void refresh(RefreshMessage message) {
    System.out.println("refresh");
    Date startDate = Utils.toSqlDate(filterStartDate.getValue());
    Date endDate = Utils.toSqlDate(filterEndDate.getValue());
    
    List paymentLines = 
        ProjectBudgetLimitWrapper.getProjectBudgetLimits(
            project,
            startDate,
            endDate);
    ProjectBudgetLimitWrapper incomingItem = new ProjectBudgetLimitWrapper(project.getIncomeType().getName());
    for (ProjectSourceWrapper source : ProjectSourceWrapper.getProjectSources(project, startDate, endDate)) {
      incomingItem.addAmounts(source.getGrantCurrencyAmount(), source.getAccountingCurrencyAmount());
    }
    
    ExpenseTypeWrapper.createBudgetSummaryList(
        paymentLines,
        Arrays.asList((ExpenseTypeWrapper)incomingItem),
        table.getItems());
    
    spentAccountingCurrencyColumn.setText(project.getAccountCurrency().getCode());
    spentGrantCurrencyColumn.setText(project.getGrantCurrency().getCode());
    remainingAccountingCurrencyColumn.setText(project.getAccountCurrency().getCode());
    remainingGrantCurrencyColumn.setText(project.getGrantCurrency().getCode());
    budgetAccountingCurrencyColumn.setText(project.getAccountCurrency().getCode());
    budgetGrantCurrencyColumn.setText(project.getGrantCurrency().getCode());
  }

  @Override
  public boolean forMe(RefreshMessage message) {
    return message.getSourceProject() == project;
  }
}
