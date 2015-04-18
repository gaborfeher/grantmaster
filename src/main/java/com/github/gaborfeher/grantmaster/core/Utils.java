package com.github.gaborfeher.grantmaster.core;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.TypedQuery;

public class Utils {
  
  public static <T extends Object> T getSingleResultWithDefault(T defaultValue, TypedQuery<T> query) {
    List<T> list = query.getResultList();
    if (list.isEmpty()) {
      return defaultValue;
    }
    if (list.size() > 1) {
      System.err.println("too long list");
    }
    return list.get(0);    
  }
  
  public static Date toSqlDate(LocalDate date) {
    if (date == null) {
      return null;
    }
    return new Date(date.toEpochDay() * 24 * 60 * 60 * 1000);
  }
}
