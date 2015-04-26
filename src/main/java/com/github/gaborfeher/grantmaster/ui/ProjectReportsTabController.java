package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectReportWrapper;
import java.util.List;
import javax.persistence.EntityManager;

public class ProjectReportsTabController extends ControllerBase {
  Project project;
    
  void init(Project project) {
    this.project = project;
  }
  
  @Override
  protected EntityWrapper createNewEntity(EntityManager em) {
    return ProjectReportWrapper.createNew(project);
  }

  @Override
  protected void getItemListForRefresh(EntityManager em, List items) {
    items.addAll(ProjectReportWrapper.getProjectReports(em, project));
    
  }

}
