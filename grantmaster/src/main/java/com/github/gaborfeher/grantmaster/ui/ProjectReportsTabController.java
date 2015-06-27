package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.framework.base.TablePageControllerBase;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.framework.base.EntityWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectReportWrapper;
import java.util.List;
import javax.persistence.EntityManager;

public class ProjectReportsTabController extends TablePageControllerBase {
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
