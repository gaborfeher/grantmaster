package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectReport;
import com.github.gaborfeher.grantmaster.logic.wrappers.BudgetCategoryWrapperBase;
import com.github.gaborfeher.grantmaster.logic.wrappers.GlobalBudgetCategoryWrapper;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectBudgetCategoryWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectReportWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectSourceWrapper;
import java.math.BigDecimal;
import javafx.event.Event;
import javafx.scene.control.ChoiceBox;
import javax.persistence.EntityManager;

public class ProjectBudgetCategoriesTabController extends ControllerBase<ProjectBudgetCategoryWrapper> {  
  @FXML TableColumn<GlobalBudgetCategoryWrapper, BigDecimal> spentGrantCurrencyColumn;
  @FXML TableColumn<GlobalBudgetCategoryWrapper, BigDecimal> spentAccountingCurrencyColumn;  
  @FXML TableColumn<GlobalBudgetCategoryWrapper, BigDecimal> remainingGrantCurrencyColumn;
  @FXML TableColumn<GlobalBudgetCategoryWrapper, BigDecimal> remainingAccountingCurrencyColumn;
  @FXML TableColumn<GlobalBudgetCategoryWrapper, BigDecimal> budgetGrantCurrencyColumn;
  @FXML TableColumn<GlobalBudgetCategoryWrapper, BigDecimal> budgetAccountingCurrencyColumn;
  
  @FXML ChoiceBox<ProjectReport> filterReport;
  
  /**
   * Project currently being open.
   */
  Project project;
  
  @Override
  protected ProjectBudgetCategoryWrapper createNewEntity(EntityManager em) {
    return ProjectBudgetCategoryWrapper.createNew(project);
  }
  
  public void filterUpdateAction(Event event) {
    onRefresh();
  }
  
  public void filterResetButtonAction(ActionEvent event) {
    filterReport.setValue(null);
    onRefresh();
  }
  
  void init(Project project) {
    this.project = project;
  }
  
  @Override
  public void getItemListForRefresh(EntityManager em, List items) {
    ProjectReport report = filterReport.getValue();
    
    List paymentLines = 
        ProjectBudgetCategoryWrapper.getProjectBudgetLimits(
            em,
            project,
            report);
    BudgetCategoryWrapperBase.createBudgetSummaryList(em, paymentLines, "Összes projektbevétel és -költség", items);
    if (items.size() > 0) {
      ProjectBudgetCategoryWrapper lastLine = (ProjectBudgetCategoryWrapper) items.get(items.size() - 1);
      for (ProjectSourceWrapper source : ProjectSourceWrapper.getProjectSources(em, project, report)) {
        lastLine.addBudgetAmounts(source.getEntity().getAccountingCurrencyAmount(), source.getEntity().getGrantCurrencyAmount());
      }
    }

    spentAccountingCurrencyColumn.setText(project.getAccountCurrency().getCode());
    spentGrantCurrencyColumn.setText(project.getGrantCurrency().getCode());
    remainingAccountingCurrencyColumn.setText(project.getAccountCurrency().getCode());
    remainingGrantCurrencyColumn.setText(project.getGrantCurrency().getCode());
    budgetAccountingCurrencyColumn.setText(project.getAccountCurrency().getCode());
    budgetGrantCurrencyColumn.setText(project.getGrantCurrency().getCode());
    
    ProjectReport reportValue = filterReport.getValue();
    filterReport.getItems().clear();
    filterReport.getItems().add(null);
    filterReport.getItems().addAll(ProjectReportWrapper.getProjectReportsWithoutWrapping(em, project));
    filterReport.setValue(reportValue);
  }

}
