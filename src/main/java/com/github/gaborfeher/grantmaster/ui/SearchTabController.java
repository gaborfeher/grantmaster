package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.core.RefreshControlSingleton;
import com.github.gaborfeher.grantmaster.core.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.wrappers.BudgetCategoryWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectExpenseWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectWrapper;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Callback;

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
    searchResults = ProjectExpenseWrapper.getExpenseList(
        project.getValue(),
        Utils.toSqlDate(startDate.getValue()),
        Utils.toSqlDate(endDate.getValue()),
        budgetCategory.getValue(),
        budgetCategoryGroup.getText(),
        accountNo.getText(),
        partnerName.getText(),
        comment1.getText(),
        comment2.getText());
    table.getItems().setAll(searchResults);
  }

  @Override
  public void refresh() {
    budgetCategory.getItems().clear();
    budgetCategory.getItems().add(null);
    budgetCategory.getItems().addAll(BudgetCategoryWrapper.getBudgetCategories(BudgetCategory.Direction.PAYMENT));
    project.getItems().clear();
    project.getItems().add(null);
    project.getItems().addAll(ProjectWrapper.getProjectsWithoutWrapping());
    
    tableController.accountingCurrencyAmountColumn.setCellValueFactory(
        new Callback<TableColumn.CellDataFeatures<ProjectExpenseWrapper, Object>, ObservableValue<Object>>() {
      @Override
      public ObservableValue<Object> call(TableColumn.CellDataFeatures<ProjectExpenseWrapper, Object> p) {
        String result =
            String.format("%2.2f %s", p.getValue().getAccountingCurrencyAmount(), p.getValue().getProject().getAccountCurrency().getCode());
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
            String.format("%2.2f %s", p.getValue().getGrantCurrencyAmount(), p.getValue().getProject().getGrantCurrency().getCode());
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

    if (searchResults != null) {
      table.getItems().setAll(searchResults);
    } else {
      table.getItems().clear();
    }
  }

  @Override
  protected ProjectExpenseWrapper createNewEntity() {
    throw new UnsupportedOperationException("Not supported.");
  }
  
}
