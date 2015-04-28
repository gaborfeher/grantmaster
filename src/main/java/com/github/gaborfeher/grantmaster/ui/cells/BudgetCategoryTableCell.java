package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.core.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.wrappers.GlobalBudgetCategoryWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import javax.persistence.EntityManager;

class BudgetCategoryTableCell<S extends EntityWrapper> extends BetterChoiceBoxTableCell<S, Object> {
  BudgetCategory.Direction direction;

  public BudgetCategoryTableCell(String property, BudgetCategory.Direction direction) {
    super(new BudgetCategoryStringConverter(), property, BudgetCategory.class);
    this.direction = direction;
  }
  
  @Override
  protected void refreshChoiceItems() {
    DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      getItems().setAll(GlobalBudgetCategoryWrapper.getBudgetCategories(em, direction));
      return true;
    });
  }
  
}
