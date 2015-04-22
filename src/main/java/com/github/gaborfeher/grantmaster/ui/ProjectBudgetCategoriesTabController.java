package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectBudgetLimit;
import com.github.gaborfeher.grantmaster.core.Utils;
import com.github.gaborfeher.grantmaster.logic.wrappers.BudgetCategoryWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectBudgetCategoryWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectSourceWrapper;
import java.sql.Date;
import javafx.scene.control.DatePicker;
import javax.persistence.EntityManager;

public class ProjectBudgetCategoriesTabController extends ControllerBase {  
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
  
  @Override
  protected EntityWrapper createNewEntity() {
    ProjectBudgetLimit limit = new ProjectBudgetLimit();
    limit.setProject(project);
    ProjectBudgetCategoryWrapper wrapper = new ProjectBudgetCategoryWrapper(limit.getBudgetCategory(), 0.0, 0.0);
    wrapper.setProject(project);
    wrapper.setLimit(0.0, limit);
    return wrapper;
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
  }
  
  @Override
  public void refresh(EntityManager em) {
    Date startDate = Utils.toSqlDate(filterStartDate.getValue());
    Date endDate = Utils.toSqlDate(filterEndDate.getValue());
    
    List paymentLines = 
        ProjectBudgetCategoryWrapper.getProjectBudgetLimits(
            em,
            project,
            startDate,
            endDate);
    table.getItems().clear();
    BudgetCategoryWrapper.createBudgetSummaryList(em, paymentLines, "Összes projektbevétel és -költség", table.getItems());
    if (table.getItems().size() > 0) {
      ProjectBudgetCategoryWrapper lastLine = (ProjectBudgetCategoryWrapper) table.getItems().get(table.getItems().size() - 1);
      for (ProjectSourceWrapper source : ProjectSourceWrapper.getProjectSources(em, project, startDate, endDate)) {
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
