package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.util.StringConverter;

/**
 * Adds some user friendliness and EntityWrapper-editing capabilities for
 * the standard Java FX choicebox table cell.
 */
public abstract class BetterChoiceBoxTableCell<S extends EntityWrapper, T> extends ChoiceBoxTableCell<S, T> {
  /**
   * The property of the edited EntityWrapper which will be updated on
   * commit.
   */
  private final String property;
  /**
   * The class of object being edited here, needed by the EntityWrapper.
   */
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
    if (getEntityWrapper() != null &&  // TODO(gaborfeher): Investigate why these are needed.
        getEntityWrapper().commitEdit(property, val, valueClass)) {
      updateItem(val, false);
    }
  }
  
  @Override
  public void startEdit() {
    if (getEntityWrapper() != null &&
        getEntityWrapper().canEdit()) {
      refreshChoiceItems();
      super.startEdit();
    }
  }
  
  /**
   * Retrieves and updates the list of choice items in the choice box.
   */
  protected abstract void refreshChoiceItems();

}
