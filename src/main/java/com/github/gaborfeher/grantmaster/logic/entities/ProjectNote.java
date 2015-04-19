package com.github.gaborfeher.grantmaster.logic.entities;

import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ProjectNote {
  @Id
  private Long id;
  
  private Project project;
  
  private Timestamp entryTime;
  
  private String note;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Timestamp getEntryTime() {
    return entryTime;
  }

  public void setEntryTime(Timestamp entryTime) {
    this.entryTime = entryTime;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }
}
