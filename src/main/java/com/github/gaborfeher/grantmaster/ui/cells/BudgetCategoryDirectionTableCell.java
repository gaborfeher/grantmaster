package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;

class BudgetCategoryDirectionTableCell<S extends EntityWrapper>
    extends BetterChoiceBoxTableCell<S, BudgetCategory.Direction> {

  public BudgetCategoryDirectionTableCell(String property) {
    super(new BudgetCategoryDirectionStringConverter(), property, BudgetCategory.Direction.class);
    getItems().add(BudgetCategory.Direction.PAYMENT);
    getItems().add(BudgetCategory.Direction.INCOME);
  }

  @Override
  protected void refreshChoiceItems() {
  } 
}
