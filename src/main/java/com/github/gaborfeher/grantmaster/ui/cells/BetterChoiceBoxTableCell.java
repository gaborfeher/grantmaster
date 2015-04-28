package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.util.StringConverter;

public abstract class BetterChoiceBoxTableCell<S extends EntityWrapper, T> extends ChoiceBoxTableCell<S, T> {
  private final String property;
  private final Class valueClass;
  
  private class FocusChangeListener implements ChangeListener<Boolean> {
    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
      if (newValue) {
        startEdit();
      }
    }
  }

  public BetterChoiceBoxTableCell(
      StringConverter<T> stringConverter,
      String property,
      Class valueClass) {
    super(stringConverter);
    this.property = property;
    this.valueClass = valueClass;
    
    focusedProperty().addListener(new FocusChangeListener());
  }
  
  protected EntityWrapper getEntityWrapper() {
    return (EntityWrapper) getTableRow().getItem();
  }
  
  @Override  
  public void commitEdit(T val) {
    if (getEntityWrapper().commitEdit(property, val, valueClass)) {
      updateItem(val, false);
    }
  }
  
  @Override
  public void startEdit() {
    if (getEntityWrapper().canEdit()) {
      refreshChoiceItems();
      super.startEdit();
    }
  }
  
  protected abstract void refreshChoiceItems();

}
