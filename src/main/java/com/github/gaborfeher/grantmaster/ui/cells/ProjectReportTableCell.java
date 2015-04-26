package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.core.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectExpense;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectReport;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectSource;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectReportWrapper;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javax.persistence.EntityManager;

class ProjectReportTableCell<S> extends ChoiceBoxTableCell<S, ProjectReport> {
  String property;

  public ProjectReportTableCell(String property) {
    super(new MultiStringConverter<ProjectReport>() {
      @Override
      public String toString(ProjectReport object) {
        return object.toString();
      }
      @Override
      public ProjectReport fromString(String string) {
        throw new UnsupportedOperationException("Not supported.");
      }
    });
    this.property = property;
  }
  
  private EntityWrapper getEntityWrapper() {
    return (EntityWrapper) getTableRow().getItem();
  }

  @Override  
  public void commitEdit(ProjectReport val) {
    if (getEntityWrapper().commitEdit(property, val, ProjectReport.class)) {
      updateItem(val, false);
    }
  }     

  @Override
  public void startEdit() {
    if (getEntityWrapper().canEdit()) {
      DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
        Project project;
        EntityWrapper entity = getEntityWrapper();
        if (entity.getEntity() instanceof ProjectSource) {
          project = ((ProjectSource) entity.getEntity()).getProject();
        } else if (entity.getEntity() instanceof ProjectExpense) {
          project = ((ProjectExpense) entity.getEntity()).getProject();
        } else {
          throw new RuntimeException("Not supported yet.");
        }
        getItems().setAll(ProjectReportWrapper.getProjectReportsWithoutWrapping(em, project));
        return true;
      });
      super.startEdit();
      
    }
  }  
}
