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
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectNote;
import java.util.List;
import javax.persistence.EntityManager;

public class ProjectNoteWrapper extends EntityWrapper {
  public ProjectNoteWrapper(ProjectNote note) {
    super(note);
  }
  
  public static List<ProjectNoteWrapper> getNotes(EntityManager em, Project project) {
    return em.createQuery(
            "SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ProjectNoteWrapper(n) " +
            "FROM ProjectNote n " +
            "WHERE n.project = :project " +
            "ORDER BY n.entryTime DESC",
            ProjectNoteWrapper.class).
        setParameter("project", project).
        getResultList();
  }
  
  static void removeProjectNotes(EntityManager em, Project project) {
    em.createQuery("DELETE FROM ProjectNote n WHERE n.project = :project").
        setParameter("project", project).
        executeUpdate();
  }
}
