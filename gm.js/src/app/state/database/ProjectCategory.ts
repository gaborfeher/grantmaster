import {Record} from 'immutable';

import {BigNumber} from 'app/state/core/BigNumber';
import {Changes} from 'app/state/core/Changes';
import {ListItem} from 'app/state/core/ListItem';

class ProjectCategoryRecord extends Record({
  id: -1,
  tagName: '',
  limitForeign: undefined,
  limitPercentageForeign: undefined,
  spentForeign: new BigNumber(0.0),
  spentLocal: new BigNumber(0.0),
  overshoot: false
}) {}

export class ProjectCategory extends ProjectCategoryRecord implements ListItem {
  id: number;
  tagName: string;
  limitForeign: BigNumber;
  limitPercentageForeign: BigNumber;

  spentForeign: BigNumber;
  spentLocal: BigNumber;

  overshoot: boolean;

  reset(): ProjectCategory {
    var that: ProjectCategory = this;
    return that.merge({
      spentLocal: new BigNumber(0.0),
      spentForeign: new BigNumber(0.0),
      overshoot: false
    });
  }

  addSpentAmounts(local: BigNumber, foreign: BigNumber) {
    var that: ProjectCategory = this;
    that = that.merge({
      spentLocal: that.spentLocal.plus(local),
      spentForeign: that.spentForeign.plus(foreign)
    });
    let overshoot = false;
    if (that.limitForeign !== undefined) {
      overshoot = overshoot || that.limitForeign.lessThan(that.spentForeign);
    }
    if (that.limitPercentageForeign !== undefined) {
      // TODO
    }
    return that.merge({
      overshoot: overshoot
    });
  }

  validate(): String[] {
    let errors = [];
    // TODO: what to validate?
    return errors;
  }

}
