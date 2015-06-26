package com.github.gaborfeher.grantmaster.ui.framework;

import com.github.gaborfeher.grantmaster.logic.entities.EntityBase;

/**
 * Objects shown in table managed by TablePageControllerBase must implement
 * this interface.
 */
public interface EditableTableRowItem {
  /**
   * @return Editing state of current row.
   */
  RowEditState getState();
  /**
   * @param state Editing state of current row. 
   */
  void setState(RowEditState state);
  /**
   * @param parent Specify the parent controller holding the table which holds
   * this objects.
   */
  void setParent(TablePageControllerBase parent);
  /**
   * Requests the parent controller to refresh the table holding this item.
   */
  void requestTableRefresh();
  /**
   * @return Is this an editable row.
   */
  boolean canEdit();
  /**
   * Change a property of the entity. Flushing the change to the underlying
   * persistence provided may depend on editing state or other factors.
   */
  boolean commitEdit(String propertyName, Object value, Class<?> valueClass);
  /**
   * @return The value of the given property.
   */
  Object getProperty(String properyName);
  /**
   * @return True if the row represents a summary line, used for styling.
   */
  boolean getIsSummary();
  /**
   * 
   * @param showErrorDialog
   * @return 
   */
  boolean validate(boolean showErrorDialog);
  /**
   * @return The encapsulated entity in this row. TODO(gaborfeher): Eliminate the need for this.
   */
  EntityBase getEntity();
}
