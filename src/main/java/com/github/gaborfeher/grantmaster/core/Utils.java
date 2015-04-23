package com.github.gaborfeher.grantmaster.core;

import java.math.MathContext;
import java.util.List;
import javax.persistence.TypedQuery;

public class Utils {
  // Note: the database has a different scale/precision setting for bigdecimals,
  // and possible different rounding rules.
  public static MathContext MC = MathContext.DECIMAL128;

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

  public static boolean prepareForEditing() {
 /*   if (RefreshControlSingleton.getInstance().isEditingActive()) {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle("Szerkesztés");
      alert.setHeaderText("Egyszerre egy dolgot lehet szerkeszteni");
      alert.setContentText("Ha ezt folytatod, az előző megnyitott\nszerkesztés adatai el fognak veszni.");
      Optional<ButtonType> result = alert.showAndWait();
      if (result.get() == ButtonType.OK) {
        RefreshControlSingleton.getInstance().broadcastRefresh();
        // There is no point in going forward here.
        return false;
      }
    }
    RefreshControlSingleton.getInstance().setEditingActive(true); */
    return true;
  }

}
