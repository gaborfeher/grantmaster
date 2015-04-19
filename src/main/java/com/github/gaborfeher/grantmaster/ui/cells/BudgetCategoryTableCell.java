package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.wrappers.BudgetCategoryWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import javafx.scene.control.cell.ChoiceBoxTableCell;

class BudgetCategoryTableCell<S extends EntityWrapper> extends ChoiceBoxTableCell<S, Object> {
  String property;
  BudgetCategory.Direction direction;

  public BudgetCategoryTableCell(String property, BudgetCategory.Direction direction) {
    super(new BudgetCategoryStringConverter());
    this.property = property;
    this.direction = direction;
  }
    
  private EntityWrapper getEntityWrapper() {
    return (EntityWrapper) getTableRow().getItem();
  }

  @Override  
  public void commitEdit(Object val) {
    if (getEntityWrapper().setPropeprty(property, (BudgetCategory) val)) {
      updateItem(val, false);
    }
  }

  @Override
  public void startEdit() {
    if (getEntityWrapper() != null && getEntityWrapper().canEdit()) {
      getItems().setAll(BudgetCategoryWrapper.getBudgetCategories(direction));
      super.startEdit();
    }
  }
  
}
