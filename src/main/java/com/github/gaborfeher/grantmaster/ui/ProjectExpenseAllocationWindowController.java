/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectExpense;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectSource;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javax.persistence.TypedQuery;

/**
 * FXML Controller class
 *
 * @author gabor
 */
public class ProjectExpenseAllocationWindowController implements Initializable {

  @FXML TableView<ProjectSource> projectTransfersTable;
  
  DatabaseConnectionSingleton databaseConnection;
  Project project;

  public ProjectExpenseAllocationWindowController() {
    databaseConnection = DatabaseConnectionSingleton.getInstance();
  }
  
  void init(Project project, ProjectExpense expense) {
    this.project = project;
  }
  
  /**
   * Initializes the controller class.
   */
  @Override
  public void initialize(URL url, ResourceBundle rb) {
    // TODO
  }
  
  void refresh() {
    TypedQuery<ProjectSource> query = databaseConnection.em().createQuery("SELECT s FROM ProjectSource s WHERE s.project = :project", ProjectSource.class);
    query.setParameter("project", project);
    List<ProjectSource> projectTransfers = query.getResultList();

    
    projectTransfersTable.getItems().setAll(projectTransfers);
  }  
  
  public void projectAllocationSubmitManualAction(ActionEvent event) {
    Stage stage = (Stage) projectTransfersTable.getScene().getWindow();
    stage.close();
  }
}
