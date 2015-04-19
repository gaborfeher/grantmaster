package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.core.RefreshControlSingleton;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

/**
 * FXML Controller class
 *
 * @author gabor
 */
public class ProjectTabController implements Initializable {
  @FXML ProjectExpenseTabController projectExpenseTabController;
  @FXML ProjectSourceTabController projectSourceTabController;
  @FXML ProjectBudgetLimitsTabController projectBudgetLimitsTabController;
  @FXML ProjectNotesTabController projectNotesTabController;

  @Override
  public void initialize(URL url, ResourceBundle rb) {
  }


  void init(Project project) {   
    this.projectExpenseTabController.init(project);
    this.projectSourceTabController.init(project);
    this.projectBudgetLimitsTabController.init(project);
    this.projectNotesTabController.init(project);
  }


}
