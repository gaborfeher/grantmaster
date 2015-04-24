package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectSource;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectSourceWrapper;
import java.math.BigDecimal;
import javax.persistence.EntityManager;

public class ProjectSourceTabController extends ControllerBase<ProjectSourceWrapper> {
  @FXML TableColumn<ProjectSourceWrapper, Float> accountingCurrencyAmountColumn;
  @FXML TableColumn<ProjectSourceWrapper, Float> grantCurrencyAmountColumn;
  @FXML TableColumn<ProjectSourceWrapper, Float> usedAccountingCurrencyAmountColumn;
  @FXML TableColumn<ProjectSourceWrapper, Float> usedGrantCurrencyAmountColumn;
  @FXML TableColumn<ProjectSourceWrapper, Float> remainingAccountingCurrencyAmountColumn;
  @FXML TableColumn<ProjectSourceWrapper, Float> remainingGrantCurrencyAmountColumn;
  
  private Project project;

  public ProjectSourceTabController() {
  }

  void init(Project project) {
    this.project = project;
  }

  @Override
  public void refresh(EntityManager em, List<ProjectSourceWrapper> items) {
    List<ProjectSourceWrapper> projectTransfers = ProjectSourceWrapper.getProjectSources(em, project, null, null);
    items.addAll(projectTransfers);
    
    String grantCurrency = project.getGrantCurrency().getCode();
    String accountingCurrency = project.getAccountCurrency().getCode();
    
    accountingCurrencyAmountColumn.setText(accountingCurrency);
    grantCurrencyAmountColumn.setText(grantCurrency);
    usedAccountingCurrencyAmountColumn.setText(accountingCurrency);
    usedGrantCurrencyAmountColumn.setText(grantCurrency);
    remainingAccountingCurrencyAmountColumn.setText(accountingCurrency);
    remainingGrantCurrencyAmountColumn.setText(grantCurrency);
  }

  @Override
  protected ProjectSourceWrapper createNewEntity() {
    ProjectSource newSource = new ProjectSource();
    newSource.setProject(project);
    return new ProjectSourceWrapper(newSource, BigDecimal.ZERO);
  }
}
