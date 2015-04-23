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
import java.math.BigDecimal;
import java.time.LocalDate;
import javafx.scene.control.DatePicker;
import javax.persistence.EntityManager;

public class ProjectBudgetCategoriesTabController extends ControllerBase {  
  @FXML TableColumn<BudgetCategoryWrapper, BigDecimal> spentGrantCurrencyColumn;
  @FXML TableColumn<BudgetCategoryWrapper, BigDecimal> spentAccountingCurrencyColumn;  
  @FXML TableColumn<BudgetCategoryWrapper, BigDecimal> remainingGrantCurrencyColumn;
  @FXML TableColumn<BudgetCategoryWrapper, BigDecimal> remainingAccountingCurrencyColumn;
  @FXML TableColumn<BudgetCategoryWrapper, BigDecimal> budgetGrantCurrencyColumn;
  @FXML TableColumn<BudgetCategoryWrapper, BigDecimal> budgetAccountingCurrencyColumn;
  
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
    ProjectBudgetCategoryWrapper wrapper = new ProjectBudgetCategoryWrapper(limit.getBudgetCategory(), BigDecimal.ZERO, BigDecimal.ZERO);
    wrapper.setProject(project);
    wrapper.setLimit(BigDecimal.ZERO, limit);
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
    LocalDate startDate = filterStartDate.getValue();
    LocalDate endDate = filterEndDate.getValue();
    
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
