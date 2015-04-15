/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.entities.ProjectExpense;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;


public class PaymentAllocationTableCellFactory<S> implements Callback<TableColumn<S, Double>, TableCell<S, Double>> {  
  private ProjectExpense expense;
  
  @Override  
  public TableCell<S, Double> call(TableColumn<S, Double> param) {  
     return new PaymentAllocationTableCell<S>(expense);  
  }        

  /**
   * @return the expense
   */
  public ProjectExpense getExpense() {
    return expense;
  }

  /**
   * @param expense the expense to set
   */
  public void setExpense(ProjectExpense expense) {
    this.expense = expense;
  }

}
