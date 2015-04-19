package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.ExpenseType;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import javafx.scene.control.cell.ChoiceBoxTableCell;

class ExpenseTypeTableCell<S extends EntityWrapper> extends ChoiceBoxTableCell<S, Object> {
  String property;
  ExpenseType.Direction direction;

  public ExpenseTypeTableCell(String property, ExpenseType.Direction direction) {
    super(new ExpenseTypeStringConverter());
    this.property = property;
    this.direction = direction;
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
    if (getEntityWrapper() != null && getEntityWrapper().canEdit()) {
      getItems().setAll(DatabaseConnectionSingleton.getInstance().em().
          createQuery(
              "SELECT t FROM ExpenseType t WHERE t.direction = :direction ORDER BY t.name",
              ExpenseType.class).
          setParameter("direction", direction).
          getResultList());
      super.startEdit();
    }
  }
  
}
