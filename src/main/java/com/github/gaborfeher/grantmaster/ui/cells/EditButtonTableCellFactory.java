/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.entities.ExpenseType;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 *
 * @author gabor
 */
public class EditButtonTableCellFactory<S extends EntityWrapper> implements Callback<TableColumn<S, EntityWrapper.State>, TableCell<S, EntityWrapper.State>> {

  @Override
  public TableCell<S, EntityWrapper.State> call(TableColumn<S, EntityWrapper.State> p) {
    return new EditButtonTableCell<S>();
  }
 
  
}
