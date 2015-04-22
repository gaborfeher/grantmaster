package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.core.TransactionRunner;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.wrappers.BudgetCategoryWrapper;
import com.github.gaborfeher.grantmaster.ui.cells.DoubleTableCellFactory;
import com.github.gaborfeher.grantmaster.ui.cells.EntityPropertyValueFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import javax.persistence.EntityManager;

public class BudgetCategoriesTabController extends ControllerBase<BudgetCategoryWrapper> {

  public BudgetCategoriesTabController() {
  }
  
  @Override
  protected void refresh(EntityManager em) {
    List<BudgetCategoryWrapper> paymentCategories = new ArrayList<>();
    List<BudgetCategoryWrapper> incomeCategories = new ArrayList<>();
    Set<String> columnNames = new TreeSet<>();
        BudgetCategoryWrapper.getYearlyBudgetCategorySummaries(
            em, paymentCategories, incomeCategories, columnNames);
    
    // Initialize table columns.
    table.getColumns().remove(4, table.getColumns().size());
    for (final String columnName : columnNames) {
      TableColumn column = new TableColumn<BudgetCategoryWrapper, Double>(columnName);
      column.setCellValueFactory(new EntityPropertyValueFactory(columnName));
      column.setCellFactory(new DoubleTableCellFactory());
      column.getStyleClass().add("numColumn");
      column.setSortable(false);
      column.setEditable(false);
      table.getColumns().add(column);
    }
    
    BudgetCategoryWrapper.createBudgetSummaryList(
        em,
        paymentCategories,
        incomeCategories,
        table.getItems());
  }

  @Override
  public BudgetCategoryWrapper createNewEntity() {
    BudgetCategory budgetCategory = new BudgetCategory();
    return new BudgetCategoryWrapper(budgetCategory);
  }

}
