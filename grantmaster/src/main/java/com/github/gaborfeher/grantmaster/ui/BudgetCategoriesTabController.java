/**
 * This file is a part of GrantMaster.
 * Copyright (C) 2015  Gábor Fehér <feherga@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.framework.base.TablePageControllerBase;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.wrappers.BudgetCategoryWrapperBase;
import com.github.gaborfeher.grantmaster.logic.wrappers.GlobalBudgetCategoryWrapper;
import com.github.gaborfeher.grantmaster.framework.ui.cells.BigDecimalTableCellFactory;
import com.github.gaborfeher.grantmaster.framework.ui.cells.EntityPropertyValueFactory;
import com.github.gaborfeher.grantmaster.framework.utils.Utils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javafx.scene.control.TableColumn;
import javax.persistence.EntityManager;

public class BudgetCategoriesTabController extends TablePageControllerBase<GlobalBudgetCategoryWrapper> {
  static final int NUM_FIXED_COLUMNS = 3;

  public BudgetCategoriesTabController() {
  }

  private void addTableColumn(List columns, String columnName) {
    TableColumn column = new TableColumn<GlobalBudgetCategoryWrapper, BigDecimal>(columnName);
    column.setCellValueFactory(new EntityPropertyValueFactory(columnName));
    column.setCellFactory(new BigDecimalTableCellFactory());
    column.getStyleClass().add("numColumn");
    column.setSortable(false);
    column.setEditable(false);
    column.setPrefWidth(130.0);
    columns.add(column);
  }

  private void addTableColumns(String mainColumnName, Set<String> columnNames) {
    TableColumn mainColumn = new TableColumn(mainColumnName);
    getTableColumns().add(mainColumn);
    for (final String columnName : columnNames) {
      addTableColumn(mainColumn.getColumns(), columnName);
    }
  }

  @Override
  protected void getItemListForRefresh(EntityManager em, List items) {
    List paymentCategories = new ArrayList();
    List incomeCategories = new ArrayList();
    Set<String> expenseColumnNames = new TreeSet<>();
    GlobalBudgetCategoryWrapper.getYearlyBudgetCategorySummaries(
        em, paymentCategories, incomeCategories, expenseColumnNames);
    Set<String> limitColumnNames = new TreeSet<>();
    GlobalBudgetCategoryWrapper.addCurrentBudgetLimitSums(
        em, paymentCategories, limitColumnNames);

    // Initialize table columns.
    getTableColumns().remove(NUM_FIXED_COLUMNS + 1, getTableColumns().size());
    addTableColumns(
        Utils.getString("BudgetCategoriesTab.LimitColumnGroup"),
        limitColumnNames);
    addTableColumns(
        Utils.getString("BudgetCategoriesTab.TransferColumnGroup"),
        expenseColumnNames);

    BudgetCategoryWrapperBase.createBudgetSummaryList(
        em,
        paymentCategories,
        incomeCategories,
        limitColumnNames,
        items);
  }

  @Override
  public GlobalBudgetCategoryWrapper createNewEntity(EntityManager em) {
    BudgetCategory budgetCategory = new BudgetCategory();
    return new GlobalBudgetCategoryWrapper(budgetCategory);
  }

}
