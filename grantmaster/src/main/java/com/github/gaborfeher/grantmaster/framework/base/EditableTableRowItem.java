/*
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
package com.github.gaborfeher.grantmaster.framework.base;

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
   * @return true if pre-persist validations were successful. Some entities
   * may want to do post-persist validations in addition to this.
   */
  boolean validate();
  /**
   * @return The encapsulated entity in this row. TODO(gaborfeher): Eliminate the need for this.
   */
  EntityBase getEntity();
}
