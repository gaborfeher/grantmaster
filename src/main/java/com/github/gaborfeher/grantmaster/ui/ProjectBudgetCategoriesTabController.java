package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectBudgetLimit;
import com.github.gaborfeher.grantmaster.core.RefreshControlSingleton;
import com.github.gaborfeher.grantmaster.core.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
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
   * Project currently being open.
   */
  Project project;
  
  public void createButtonAction(ActionEvent event) {
    ProjectBudgetLimit limit = new ProjectBudgetLimit();
    limit.setProject(project);
    ProjectBudgetCategoryWrapper wrapper = new ProjectBudgetCategoryWrapper(limit.getBudgetCategory(), 0.0, 0.0);
    wrapper.setProject(project);
    wrapper.setLimit(0.0, limit);
    Utils.addNewEntityForEditing(wrapper, table.getItems());
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
    Date startDate = Utils.toSqlDate(filterStartDate.getValue());
    Date endDate = Utils.toSqlDate(filterEndDate.getValue());
    
    List paymentLines = 
        ProjectBudgetCategoryWrapper.getProjectBudgetLimits(
            project,
            startDate,
            endDate);
    table.getItems().clear();
    BudgetCategoryWrapper.createBudgetSummaryList(paymentLines, "Összes projektbevétel és -költség", table.getItems());
    if (table.getItems().size() > 0) {
      ProjectBudgetCategoryWrapper lastLine = (ProjectBudgetCategoryWrapper) table.getItems().get(table.getItems().size() - 1);
      for (ProjectSourceWrapper source : ProjectSourceWrapper.getProjectSources(project, startDate, endDate)) {
        lastLine.addBudgetAmounts(source.getAccountingCurrencyAmount(), source.getGrantCurrencyAmount());
      }
    }

    spentAccountingCurrencyColumn.setText(project.getAccountCurrency().getCode());
    spentGrantCurrencyColumn.setText(project.getGrantCurrency().getCode());
    remainingAccountingCurrencyColumn.setText(project.getAccountCurrency().getCode());
    remainingGrantCurrencyColumn.setText(project.getGrantCurrency().getCode());
    budgetAccountingCurrencyColumn.setText(project.getAccountCurrency().getCode());
    budgetGrantCurrencyColumn.setText(project.getGrantCurrency().getCode());
  }
}
