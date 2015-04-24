package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.core.TransactionRunner;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.wrappers.GlobalBudgetCategoryWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectExpenseWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectWrapper;
import java.util.List;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import javax.persistence.EntityManager;

public class SearchTabController
    extends ControllerBase<ProjectExpenseWrapper> {

  @FXML ExpenseTableController tableController;
  
  @FXML ChoiceBox<Project> project;
  @FXML DatePicker startDate;
  @FXML DatePicker endDate;
  @FXML ChoiceBox<BudgetCategory> budgetCategory;
  @FXML TextField budgetCategoryGroup;
  @FXML TextField accountNo;
  @FXML TextField partnerName;
  @FXML TextField comment1;
  @FXML TextField comment2;
  
  List<ProjectExpenseWrapper> searchResults;
  
  public void search() {
    DatabaseConnectionSingleton.getInstance().runWithEntityManager(new TransactionRunner() {

      @Override
      public boolean run(EntityManager em) {
        searchResults = ProjectExpenseWrapper.getExpenseList(
            em,
            project.getValue(),
            startDate.getValue(),
            endDate.getValue(),
            budgetCategory.getValue(),
            budgetCategoryGroup.getText(),
            accountNo.getText(),
            partnerName.getText(),
            comment1.getText(),
            comment2.getText());
        table.getItems().setAll(searchResults);
        return true;
      }
      
    });
    
  }

  @Override
  public void refresh(EntityManager em, List<ProjectExpenseWrapper> items) {
    budgetCategory.getItems().clear();
    budgetCategory.getItems().add(null);
    budgetCategory.getItems().addAll(GlobalBudgetCategoryWrapper.getBudgetCategories(em, BudgetCategory.Direction.PAYMENT));
    project.getItems().clear();
    project.getItems().add(null);
    project.getItems().addAll(ProjectWrapper.getProjectsWithoutWrapping(em));
    
    tableController.accountingCurrencyAmountColumn.setCellValueFactory(
        new Callback<TableColumn.CellDataFeatures<ProjectExpenseWrapper, Object>, ObservableValue<Object>>() {
      @Override
      public ObservableValue<Object> call(TableColumn.CellDataFeatures<ProjectExpenseWrapper, Object> p) {
        String result =
            String.format("%2.2f %s", p.getValue().getProperty("accountingCurrencyAmount"), p.getValue().getProject().getAccountCurrency().getCode());
        return new ReadOnlyObjectWrapper<Object>(result);
      }
    });
    tableController.accountingCurrencyAmountColumn.setCellFactory(
        new Callback<TableColumn<ProjectExpenseWrapper, Object>, TableCell<ProjectExpenseWrapper, Object>>() {
      @Override
      public TableCell<ProjectExpenseWrapper, Object> call(TableColumn<ProjectExpenseWrapper, Object> p) {
        return (TableCell<ProjectExpenseWrapper, Object>) TableColumn.DEFAULT_CELL_FACTORY.call(p);
      }
    });
       
    tableController.grantCurrencyAmountColumn.setCellValueFactory(
        new Callback<TableColumn.CellDataFeatures<ProjectExpenseWrapper, Object>, ObservableValue<Object>>() {
      @Override
      public ObservableValue<Object> call(TableColumn.CellDataFeatures<ProjectExpenseWrapper, Object> p) {
        String result =
            String.format("%2.2f %s", p.getValue().getProperty("grantCurrencyAmount"), p.getValue().getProject().getGrantCurrency().getCode());
        return new ReadOnlyObjectWrapper<Object>(result);
      }
    });
    tableController.grantCurrencyAmountColumn.setCellFactory(
        new Callback<TableColumn<ProjectExpenseWrapper, Object>, TableCell<ProjectExpenseWrapper, Object>>() {
      @Override
      public TableCell<ProjectExpenseWrapper, Object> call(TableColumn<ProjectExpenseWrapper, Object> p) {
        return (TableCell<ProjectExpenseWrapper, Object>) TableColumn.DEFAULT_CELL_FACTORY.call(p);
      }
    });

    items.clear();
    if (searchResults != null) {
      items.addAll(searchResults);
    }
  }

  @Override
  protected ProjectExpenseWrapper createNewEntity() {
    return null;
  }
  
}
