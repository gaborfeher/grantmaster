package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.ui.framework.TablePageControllerBase;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.wrappers.BudgetCategoryWrapperBase;
import com.github.gaborfeher.grantmaster.logic.wrappers.GlobalBudgetCategoryWrapper;
import com.github.gaborfeher.grantmaster.ui.cells.BigDecimalTableCellFactory;
import com.github.gaborfeher.grantmaster.ui.cells.EntityPropertyValueFactory;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javafx.scene.control.TableColumn;
import javax.persistence.EntityManager;

public class BudgetCategoriesTabController extends TablePageControllerBase<GlobalBudgetCategoryWrapper> {

  public BudgetCategoriesTabController() {
  }
  
  @Override
  protected void getItemListForRefresh(EntityManager em, List items) {
    List paymentCategories = new ArrayList();
    List incomeCategories = new ArrayList();
    Set<String> columnNames = new TreeSet<>();
        GlobalBudgetCategoryWrapper.getYearlyBudgetCategorySummaries(
            em, paymentCategories, incomeCategories, columnNames);
    
    // Initialize table columns.
    getTableColumns().remove(4, getTableColumns().size());
    for (final String columnName : columnNames) {
      TableColumn column = new TableColumn<GlobalBudgetCategoryWrapper, BigDecimal>(columnName);
      column.setCellValueFactory(new EntityPropertyValueFactory(columnName));
      column.setCellFactory(new BigDecimalTableCellFactory());
      column.getStyleClass().add("numColumn");
      column.setSortable(false);
      column.setEditable(false);
      column.setPrefWidth(130.0);
      getTableColumns().add(column);
    }
    
    BudgetCategoryWrapperBase.createBudgetSummaryList(
        em,
        paymentCategories,
        incomeCategories,
        items);
  }

  @Override
  public GlobalBudgetCategoryWrapper createNewEntity(EntityManager em) {
    BudgetCategory budgetCategory = new BudgetCategory();
    return new GlobalBudgetCategoryWrapper(budgetCategory);
  }

}
