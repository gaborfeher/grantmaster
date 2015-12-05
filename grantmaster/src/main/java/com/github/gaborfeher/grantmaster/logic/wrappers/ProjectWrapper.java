/**
 * This file is a part of GrantMaster.
 * Copyright (C) 2015  Gábor Fehér <feherga@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.framework.base.EntityWrapper;
import com.github.gaborfeher.grantmaster.framework.base.RowEditState;
import com.github.gaborfeher.grantmaster.framework.utils.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectReport;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityManager;

public class ProjectWrapper extends EntityWrapper<Project> {
  public ProjectWrapper(Project project) {
    super(project);
  }

  @Override
  public boolean delete(EntityManager em) {
    entity = em.find(Project.class, entity.getId());
    ProjectExpenseWrapper.removeProjectExpenses(em, entity);
    ProjectSourceWrapper.removeProjectSources(em, entity);
    ProjectBudgetCategoryWrapper.removeProjectBudgetLimits(em, entity);
    ProjectNoteWrapper.removeProjectNotes(em, entity);
    ProjectReportWrapper.removeReports(em, entity);
    em.remove(entity);
    return true;
  }

  public static List<ProjectWrapper> getProjects(EntityManager em) {
    return em.createQuery(
        "SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectWrapper(p) " +
            "FROM Project p " +
            "ORDER BY p.name",
        ProjectWrapper.class).
            getResultList();
  }

  public static List<Project> getProjectsWithoutWrapping(EntityManager em) {
    return em.createQuery("SELECT p FROM Project p ORDER BY p.name", Project.class).getResultList();
  }

  public static ProjectWrapper createNew() {
    Project newProject = new Project();
    ProjectReport projectReport = new ProjectReport();
    projectReport.setProject(newProject);
    projectReport.setReportDate(LocalDate.now());
    projectReport.setNote(Utils.getString("ProjectDefaultReportName"));
    newProject.setReports(Arrays.asList(projectReport));
    return new ProjectWrapper(newProject);
  }

  @Override
  public boolean setProperty(String name, Object value, Class<?> paramType) {
    if ("expenseMode".equals(name) && getState() != RowEditState.EDITING_NEW) {
      // This value cannot be changed once the project is created.
      return false;  // Refuse changing it.
    }
    return super.setProperty(name, value, paramType);
  }

  @Override
  protected boolean fillRandom(EntityManager em) {
    entity.setName("RND" + Utils.testRandom.nextInt(100000));
    entity.setAccountCurrency((Currency) Utils.testRandom.pickFromList(CurrencyWrapper.getCurrencyWrappers(em)).getEntity());
    entity.setGrantCurrency((Currency) Utils.testRandom.pickFromList(CurrencyWrapper.getCurrencyWrappers(em)).getEntity());
    entity.setExpenseMode(Project.ExpenseMode.NORMAL_AUTO_BY_SOURCE);
    entity.setIncomeType(GlobalBudgetCategoryWrapper.getBudgetCategories(em, BudgetCategory.Direction.INCOME).get(0));
    return true;
  }

}
