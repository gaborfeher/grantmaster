/*
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
package com.github.gaborfeher.grantmaster.logic.entities;

import com.github.gaborfeher.grantmaster.framework.base.EntityBase;
import java.io.Serializable;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

@Entity
@Table(
    uniqueConstraints=
        @UniqueConstraint(columnNames={"reportDate", "project_id"}))
public class ProjectReport extends EntityBase implements Serializable {
  @Id
  @GeneratedValue
  private long id;

  @NotNull(message="%ValidationErrorReportDateNotEmpty")
  @Column(nullable = false)
  @Temporal(TemporalType.DATE)
  private LocalDate reportDate;
  
  @ManyToOne(optional = false)
  @JoinColumn(nullable = false)
  private Project project;
  
  @Column(nullable = true)
  private String note;
  
  public static enum Status {
    OPEN,
    CLOSED;
  }
  @Column(nullable = false)
  private Status status = Status.OPEN;
  
  @Override
  public Long getId() {
    return id;
  }
  
  @Override
  public String toString() {
    return "R" + reportDate.toString();
  }

  public LocalDate getReportDate() {
    return reportDate;
  }

  public void setReportDate(LocalDate reportDate) {
    this.reportDate = reportDate;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }
  
  public Status getStatus() {
    return status;
  }
  
  public void setStatus(Status status) {
    this.status = status;
  }
}
