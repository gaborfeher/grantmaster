package com.github.gaborfeher.grantmaster.logic.entities;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.TemporalUnit;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

// TODO(gaborfeher): Use UTC instead of local time zone.
@Converter(autoApply = true)
public class LocalDatePersistenceConverter implements AttributeConverter<LocalDate, Date> {

  @Override
  public Date convertToDatabaseColumn(LocalDate entityValue) {
    if (entityValue == null) {
      return null;
    }
    return Date.valueOf(entityValue);
  }

  @Override
  public LocalDate convertToEntityAttribute(Date databaseValue) {
    if (databaseValue == null) {
      return null;
    }
    return databaseValue.toLocalDate();
  }

}
