package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectBudgetLimit;
import com.github.gaborfeher.grantmaster.core.RefreshControlSingleton;
import com.github.gaborfeher.grantmaster.core.RefreshMessage;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectSource;
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

/**
 * FXML Controller class
 *
 * @author gabor
 */
public class ProjectBudgetLimitsTabController extends RefreshControlSingleton.MessageObserver implements Initializable {  
  @FXML TableView<EntityWrapper> table;
  @FXML TableColumn<EntityWrapper, Float> budgetColumn;
  @FXML TableColumn<EntityWrapper, Float> spentColumn;
  @FXML TableColumn<EntityWrapper, Float> remainingColumn;
  
  Project project;
  ResourceBundle resourceBundle;
  
  public void createButtonAction(ActionEvent event) {
    ProjectBudgetLimit limit = new ProjectBudgetLimit();
    limit.setProject(project);
    ProjectBudgetLimitWrapper wrapper = new ProjectBudgetLimitWrapper(limit.getExpenseType(), 0.0);
    wrapper.setProject(project);
    wrapper.setLimit(0.0, limit);
    wrapper.setState(EntityWrapper.State.EDITING_NEW);
    table.getItems().add(wrapper);
  }
  
  void init(Project project) {
    this.project = project;
  }
  
  /**
   * Initializes the controller class.
   */
  @Override
  public void initialize(URL url, ResourceBundle rb) {
    RefreshControlSingleton.getInstance().subscribe(this);
    this.resourceBundle = rb;
  }  

  @Override
  public void refresh(RefreshMessage message) {
    table.getItems().clear();
    table.getItems().add(new FakeBudgetEntityWrapper("Kiadások"));
    List<ProjectBudgetLimitWrapper> projectResources = ProjectBudgetLimitWrapper.getProjectBudgetLimits(project);
    table.getItems().addAll(projectResources);
    FakeBudgetEntityWrapper spentSum = new FakeBudgetEntityWrapper("Kiadások összesen", true);
    for (ProjectBudgetLimitWrapper outgoing : projectResources) {
      spentSum.add(outgoing);
    }
    table.getItems().add(spentSum);
    table.getItems().add(new FakeBudgetEntityWrapper("Bevételek"));
    
    double sum = 0.0;
    for (ProjectSourceWrapper source : ProjectSourceWrapper.getProjectSources(project)) {
      sum += source.getGrantCurrencyAmount();
    }
    FakeBudgetEntityWrapper incomingSum = new FakeBudgetEntityWrapper("Bevételek összesen", true);
    FakeBudgetEntityWrapper incomingItem = new FakeBudgetEntityWrapper(project.getIncomeType().getName(), true);
    incomingItem.setBudget(sum);
    incomingItem.setSpent(spentSum.getSpent());
    incomingItem.setRemaining(sum - spentSum.getSpent());
    incomingSum.add(incomingItem);
    table.getItems().add(incomingItem);
    table.getItems().add(incomingSum);
    
    
    budgetColumn.setText(
        resourceBundle.getString("BudgetLimitValueColumn") + " (" + project.getGrantCurrency().getCode() + ")");
    spentColumn.setText(
        resourceBundle.getString("BudgetLimitSpentColumn") + " (" + project.getGrantCurrency().getCode() + ")");
    remainingColumn.setText(
        resourceBundle.getString("BudgetLimitRemainingColumn") + " (" + project.getGrantCurrency().getCode() + ")");
  }

  @Override
  public boolean forMe(RefreshMessage message) {
    return message.getSourceProject() == project;
  }
}
