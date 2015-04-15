package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.ui.ProjectExpenseAllocationWindowController;
import com.github.gaborfeher.grantmaster.logic.entities.ExpenseSourceAllocation;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectExpense;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectSource;
import java.util.ArrayList;
import java.util.Objects;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;

public class PaymentAllocationTableCell<S> extends TextFieldTableCell<S, Double> {
  ProjectExpense expense;
  
  public PaymentAllocationTableCell(ProjectExpense expense) {
    super((StringConverter<Double>)new DoubleStringConverter());
    this.expense = expense;
  }  

  @Override  
  public void commitEdit(Double val) {
    
    ProjectSource source = (ProjectSource)
        getTableView().getItems().get(getTableView().getEditingCell().getRow());
    ExpenseSourceAllocation allocation = null;
    if (expense.getSourceAllocations() == null) {
      expense.setSourceAllocations(new ArrayList<ExpenseSourceAllocation>());
    }
    for (ExpenseSourceAllocation a : expense.getSourceAllocations()) {
      if (a.getSource().getId() == source.getId()) {
        allocation = a;
        break;
      }
    }
    if (allocation == null) {
      allocation = new ExpenseSourceAllocation();
      allocation.setExpense(expense);
      allocation.setSource(source);
      expense.getSourceAllocations().add(allocation);
    }
    
    allocation.setAccountingCurrencyAmount(val);
    
    
    //t.setAmountToUse((Float) val);
    updateItem(val, false);
  }     

  
}
