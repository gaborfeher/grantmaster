package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.entities.ProjectReport;
import com.github.gaborfeher.grantmaster.framework.ui.cells.PropertyTableCellFactoryBase;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;


public class ProjectReportTableCellFactory<S>
    extends PropertyTableCellFactoryBase
    implements Callback<TableColumn<S, ProjectReport>, TableCell<S, ProjectReport>> {

  @Override
  public TableCell<S, ProjectReport> call(TableColumn<S, ProjectReport> param) {
    return new ProjectReportTableCell(property);
  }
  
}
