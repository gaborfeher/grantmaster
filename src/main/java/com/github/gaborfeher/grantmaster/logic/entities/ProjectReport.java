package com.github.gaborfeher.grantmaster.logic.entities;

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
  
  @Override
  public Long getId() {
    return id;
  }
  
  @Override
  public String toString() {
    return project.getName() + ":" + reportDate.toString();
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
}
