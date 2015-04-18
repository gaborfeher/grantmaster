package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectBudgetLimit;
import com.github.gaborfeher.grantmaster.core.RefreshControlSingleton;
import com.github.gaborfeher.grantmaster.core.RefreshMessage;
import com.github.gaborfeher.grantmaster.core.Utils;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.FakeBudgetEntityWrapper;
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
import javafx.scene.control.DatePicker;

public class ProjectBudgetLimitsTabController extends RefreshControlSingleton.MessageObserver implements Initializable {  
  @FXML TableView<EntityWrapper> table;
  @FXML TableColumn<EntityWrapper, Double> spentGrantCurrencyColumn;
  @FXML TableColumn<EntityWrapper, Double> spentAccountingCurrencyColumn;  
  @FXML TableColumn<EntityWrapper, Double> remainingGrantCurrencyColumn;
  @FXML TableColumn<EntityWrapper, Double> remainingAccountingCurrencyColumn;
  @FXML TableColumn<EntityWrapper, Double> budgetGrantCurrencyColumn;
  @FXML TableColumn<EntityWrapper, Double> budgetAccountingCurrencyColumn;
  
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
    
    table.getItems().clear();
    List<ProjectBudgetLimitWrapper> projectResources =
        ProjectBudgetLimitWrapper.getProjectBudgetLimits(
            project,
            startDate,
            endDate);
    table.getItems().clear();
    FakeBudgetEntityWrapper spentSum = new FakeBudgetEntityWrapper("Kiadások összesen", true);
    FakeBudgetEntityWrapper groupSum = null;
    for (ProjectBudgetLimitWrapper outgoing : projectResources) {
      if (groupSum != null && !groupSum.getGroupName().equals(outgoing.getGroupName())) {
        table.getItems().add(groupSum);
        groupSum = null;
      }
      if (groupSum == null && outgoing.getGroupName() != null) {
        groupSum = new FakeBudgetEntityWrapper(outgoing.getGroupName() + " összesen", true, outgoing.getGroupName());
      }
      table.getItems().add(outgoing);
      spentSum.add(outgoing);
      if (groupSum != null) {
        groupSum.add(outgoing);
      }
    }
    if (groupSum != null) {
      table.getItems().add(groupSum);
    }
    table.getItems().add(spentSum);
    
    double sumGrantCurrency = 0.0;
    double sumAccountingCurrency = 0.0;
    for (ProjectSourceWrapper source : ProjectSourceWrapper.getProjectSources(project, startDate, endDate)) {
      sumGrantCurrency += source.getGrantCurrencyAmount();
      sumAccountingCurrency += source.getAccountingCurrencyAmount();
    }
    FakeBudgetEntityWrapper incomingSum = new FakeBudgetEntityWrapper("Bevételek összesen", true);
    FakeBudgetEntityWrapper incomingItem = new FakeBudgetEntityWrapper(project.getIncomeType().getName(), true);
    //incomingItem.setBudgetGrantCurrency(sumGrantCurrency);
    //incomingItem.setBudgetAccountingCurrency(sumAccountingCurrency);
    incomingItem.setSpentGrantCurrency(sumGrantCurrency);
    incomingItem.setSpentAccountingCurrency(sumAccountingCurrency);
    incomingSum.add(incomingItem);
    table.getItems().add(incomingItem);
    table.getItems().add(incomingSum);
    
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
