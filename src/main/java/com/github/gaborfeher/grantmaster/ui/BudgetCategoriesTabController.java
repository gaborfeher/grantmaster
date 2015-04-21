package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.core.RefreshControlSingleton;
import com.github.gaborfeher.grantmaster.core.Utils;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.BudgetCategoryWrapper;
import com.github.gaborfeher.grantmaster.ui.cells.DoubleTableCellFactory;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class BudgetCategoriesTabController extends ControllerBase<BudgetCategoryWrapper> implements Initializable {

  public BudgetCategoriesTabController() {
  }
  
  @Override
  public void refresh() {
    List<BudgetCategoryWrapper> paymentCategories = new ArrayList<>();
    List<BudgetCategoryWrapper> incomeCategories = new ArrayList<>();
    Set<String> columnNames = new TreeSet<>();
    BudgetCategoryWrapper.getYearlyBudgetCategorySummaries(
        paymentCategories, incomeCategories, columnNames);

    // Initialize table columns.
    table.getColumns().remove(4, table.getColumns().size());
    for (final String columnName : columnNames) {
      TableColumn column = new TableColumn<BudgetCategoryWrapper, Double>(columnName);
      column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<BudgetCategoryWrapper, Double>,ObservableValue<Double>>() {
        @Override
        public ObservableValue<Double> call(TableColumn.CellDataFeatures<BudgetCategoryWrapper, Double> p) {
          Double value = p.getValue().getSummaryValue(columnName);
          return new ReadOnlyObjectWrapper<Double>(value);
        }
      });
      column.setCellFactory(new DoubleTableCellFactory());
      column.getStyleClass().add("numColumn");
      column.setSortable(false);
      column.setEditable(false);
      table.getColumns().add(column);
    }
    
    BudgetCategoryWrapper.createBudgetSummaryList(
        paymentCategories,
        incomeCategories,
        table.getItems());
  }

  /**
   * Initializes the controller class.
   */
  @Override
  public void initialize(URL url, ResourceBundle rb) {
    subscribe();
  }  

  @Override
  public BudgetCategoryWrapper createNewEntity() {
    BudgetCategory budgetCategory = new BudgetCategory();
    return new BudgetCategoryWrapper(budgetCategory);
  }

}
