package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.core.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectExpense;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectReport;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectSource;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectReportWrapper;
import com.github.gaborfeher.grantmaster.ui.framework.EditableTableRowItem;
import javax.persistence.EntityManager;

class ProjectReportTableCell<S extends EditableTableRowItem>
    extends BetterChoiceBoxTableCell<S, ProjectReport> {
  private static class ProjectReportStringConverter extends MultiStringConverter<ProjectReport> {
    @Override
    public String toString(ProjectReport object) {
      return object.toString();
    }
    @Override
    public ProjectReport fromString(String string) {
      throw new UnsupportedOperationException("Not supported.");
    }
  }
  
  public ProjectReportTableCell(String property) {
    super(new ProjectReportStringConverter(), property, ProjectReport.class);
  }

  @Override
  protected void refreshChoiceItems() {
    DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      Project project;
      EditableTableRowItem entity = getEntityWrapper();
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
  }
}
