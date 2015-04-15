/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.entities.ExpenseSourceAllocation;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectExpense;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectSource;
import java.util.Objects;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.util.Callback;

/**
 *
 * @author gabor
 */
public class PaymentAllocationTableValueFactory implements
    Callback<CellDataFeatures<ProjectSource, Double>, ObservableValue<Double>> {
  
  private ProjectExpense expense;
  
  public ObservableValue<Double> call(CellDataFeatures<ProjectSource, Double> p) {
         // p.getValue() returns the Person instance for a particular TableView row
//         return p.getValue().firstNameProperty();
    System.out.println("expense= " + expense);
    
    ProjectSource source = p.getValue();
    
    ExpenseSourceAllocation allocation = null;
    if (expense.getSourceAllocations() == null) {
      return new ReadOnlyObjectWrapper<Double>(0.0);
    }
    for (ExpenseSourceAllocation a : expense.getSourceAllocations()) {
      if (Objects.equals(a.getSource().getId(), source.getId())) {
        allocation = a;
        break;
      }
    }
    if (allocation == null) {
      return new ReadOnlyObjectWrapper<Double>(0.0);
    } else {
      return new ReadOnlyObjectWrapper<Double>(allocation.getAccountingCurrencyAmount());
    }
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
    System.out.println("setExpense " + expense);
    this.expense = expense;
  }
}
