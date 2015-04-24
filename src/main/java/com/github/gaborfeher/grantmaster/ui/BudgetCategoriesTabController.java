package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.wrappers.BudgetCategoryWrapper;
import com.github.gaborfeher.grantmaster.ui.cells.BigDecimalTableCellFactory;
import com.github.gaborfeher.grantmaster.ui.cells.EntityPropertyValueFactory;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javafx.scene.control.TableColumn;
import javax.persistence.EntityManager;

public class BudgetCategoriesTabController extends ControllerBase<BudgetCategoryWrapper> {

  public BudgetCategoriesTabController() {
  }
  
  @Override
  protected void refresh(EntityManager em, List<BudgetCategoryWrapper> items) {
    List<BudgetCategoryWrapper> paymentCategories = new ArrayList<>();
    List<BudgetCategoryWrapper> incomeCategories = new ArrayList<>();
    Set<String> columnNames = new TreeSet<>();
        BudgetCategoryWrapper.getYearlyBudgetCategorySummaries(
            em, paymentCategories, incomeCategories, columnNames);
    
    // Initialize table columns.
    table.getColumns().remove(4, table.getColumns().size());
    for (final String columnName : columnNames) {
      TableColumn column = new TableColumn<BudgetCategoryWrapper, BigDecimal>(columnName);
      column.setCellValueFactory(new EntityPropertyValueFactory(columnName));
      column.setCellFactory(new BigDecimalTableCellFactory());
      column.getStyleClass().add("numColumn");
      column.setSortable(false);
      column.setEditable(false);
      column.setPrefWidth(130.0);
      table.getColumns().add(column);
    }
    
    BudgetCategoryWrapper.createBudgetSummaryList(
        em,
        paymentCategories,
        incomeCategories,
        items);
  }

  @Override
  public BudgetCategoryWrapper createNewEntity() {
    BudgetCategory budgetCategory = new BudgetCategory();
    return new BudgetCategoryWrapper(budgetCategory);
  }

}
