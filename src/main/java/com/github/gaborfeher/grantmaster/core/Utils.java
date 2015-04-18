package com.github.gaborfeher.grantmaster.core;

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
}
