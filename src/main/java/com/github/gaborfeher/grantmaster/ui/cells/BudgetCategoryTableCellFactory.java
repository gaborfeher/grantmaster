package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class BudgetCategoryTableCellFactory<S extends EntityWrapper>
    extends PropertyTableCellFactoryBase
    implements Callback<TableColumn<S, Object>, TableCell<S, Object>> {

  private BudgetCategory.Direction direction;
  
  @Override  
  public TableCell<S, Object> call(TableColumn<S, Object> param) {  
    return new BudgetCategoryTableCell(property, getDirection());  
  }        

  /**
   * @return the direction
   */
  public BudgetCategory.Direction getDirection() {
    return direction;
  }

  /**
   * @param direction the direction to set
   */
  public void setDirection(BudgetCategory.Direction direction) {
    this.direction = direction;
  }
}
