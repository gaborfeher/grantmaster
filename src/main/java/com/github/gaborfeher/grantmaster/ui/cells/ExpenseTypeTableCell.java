package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.ExpenseType;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import javafx.scene.control.cell.ChoiceBoxTableCell;

class ExpenseTypeTableCell<S extends EntityWrapper> extends ChoiceBoxTableCell<S, Object> {
  String property;

  public ExpenseTypeTableCell(String property) {
    super(new ExpenseTypeStringConverter());
    this.property = property;
  }
    
  private EntityWrapper getEntityWrapper() {
    return (EntityWrapper) getTableRow().getItem();
  }

  @Override  
  public void commitEdit(Object val) {
    if (getEntityWrapper().setPropeprty(property, (ExpenseType) val)) {
      updateItem(val, false);
    }
  }

  @Override
  public void startEdit() {
    if (getEntityWrapper().canEdit()) {
      getItems().setAll(DatabaseConnectionSingleton.getInstance().em().createQuery("SELECT t FROM ExpenseType t", ExpenseType.class).getResultList());
      super.startEdit();
    }
  }
  
}
