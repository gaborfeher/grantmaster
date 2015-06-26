package com.github.gaborfeher.grantmaster.logic.entities;

public abstract class EntityBase {
  public abstract Long getId();

  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (!other.getClass().equals(this.getClass())) {
      return false;
    }
    EntityBase otherEntity = (EntityBase) other;
    if (this.getId() == null || otherEntity.getId() == null) {
      return false;
    }
    return this.getId().equals(otherEntity.getId());
  }

  @Override
  public int hashCode() {
    return getId() == null ? 0 : getId().hashCode();
  }
}
