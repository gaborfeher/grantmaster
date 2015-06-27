package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.framework.base.TablePageControllerBase;
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.wrappers.CurrencyWrapper;
import com.github.gaborfeher.grantmaster.framework.base.EntityWrapper;
import java.util.List;
import javax.persistence.EntityManager;

public class CurrenciesTabController extends TablePageControllerBase {
  @Override
  protected EntityWrapper createNewEntity(EntityManager em) {
    return new CurrencyWrapper(new Currency());
  }

  @Override
  protected void getItemListForRefresh(EntityManager em, List items) {
    items.addAll(CurrencyWrapper.getCurrencyWrappers(em));
  }
}
