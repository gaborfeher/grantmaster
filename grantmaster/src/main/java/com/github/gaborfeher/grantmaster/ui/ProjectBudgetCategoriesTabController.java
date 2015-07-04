/**
 * This file is a part of GrantMaster.
 * Copyright (C) 2015  Gábor Fehér <feherga@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.framework.base.TablePageControllerBase;
import java.util.List;
import java.math.BigDecimal;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.ChoiceBox;
import javax.persistence.EntityManager;

import com.github.gaborfeher.grantmaster.framework.utils.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.framework.utils.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectBudgetLimit;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectReport;
import com.github.gaborfeher.grantmaster.logic.wrappers.BudgetCategoryWrapperBase;
import com.github.gaborfeher.grantmaster.logic.wrappers.GlobalBudgetCategoryWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectBudgetCategoryWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectReportWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectSourceWrapper;

public class ProjectBudgetCategoriesTabController extends TablePageControllerBase<ProjectBudgetCategoryWrapper> {  
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
  
  public void filterResetButtonAction(ActionEvent event) {
    filterReport.setValue(null);
    onRefresh();
  }
  
  void init(Project project) {
    this.project = project;
    filterReport.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ProjectReport>() {
      @Override
      public void changed(ObservableValue<? extends ProjectReport> observable, ProjectReport oldValue, ProjectReport newValue) {
        refreshTableContent();
      }
    });
  }
  
  @Override
  public void getItemListForRefresh(EntityManager em, List items) {
    ProjectReport report = filterReport.getValue();
    
    List paymentLines = 
        ProjectBudgetCategoryWrapper.getProjectBudgetLimits(
            em,
            project,
            report);
    BudgetCategoryWrapperBase.createBudgetSummaryList(
        em, paymentLines, Utils.getString("ProjectSumIncomesAndExpenses"), items);
    ProjectBudgetCategoryWrapper lastLineSummary = null;
    if (items.size() > 0) {
      lastLineSummary = (ProjectBudgetCategoryWrapper) items.get(items.size() - 1);
    } else {
      lastLineSummary = new ProjectBudgetCategoryWrapper(
          Utils.getString("ProjectSumIncomesAndExpenses"));
      lastLineSummary.setLimit(BigDecimal.ZERO, new ProjectBudgetLimit());
      lastLineSummary.setIsSummary(true);
      lastLineSummary.setState(null);
      items.add(lastLineSummary);
    }
    for (ProjectSourceWrapper source : ProjectSourceWrapper.getProjectSources(em, project, report)) {
      lastLineSummary.addBudgetAmounts(source.getEntity().getAccountingCurrencyAmount(), source.getEntity().getGrantCurrencyAmount());
    }
  }

  @Override
  protected void refreshOtherContent() {
    DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
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
      return true;
    });
  }

}
