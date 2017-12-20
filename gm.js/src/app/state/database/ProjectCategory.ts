import {Record} from 'immutable';
import {BigNumber} from 'bignumber.js';

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

  refresh(totalIncomeForeign: BigNumber): ProjectCategory {
    if (this.limitPercentageForeign !== undefined) {
      let limitForeign = totalIncomeForeign
          .times(this.limitPercentageForeign)
          .times(0.01);
      return this.set('limitForeign', limitForeign);
    } else {
      return this;
    }
  }

  reset(): ProjectCategory {
    var that: ProjectCategory = this;
    return that.merge({
      spentLocal: new BigNumber(0.0),
      spentForeign: new BigNumber(0.0),
      overshoot: false
    });
  }

  addSpentAmounts(local: BigNumber, foreign: BigNumber) {
    let that: ProjectCategory = this.merge({
      spentLocal: this.spentLocal.plus(local),
      spentForeign: this.spentForeign.plus(foreign)
    });
    let overshoot = this.limitForeign.lessThan(that.spentForeign);
    return that.merge({
      overshoot: overshoot
    });
  }

  validate(): String[] {
    let errors = [];
    if (this.limitForeign === undefined &&
        this.limitPercentageForeign === undefined) {
      errors.push('you must specify a limit (either absolute or percentage)');
    }
    // TODO: what to validate?
    return errors;
  }

}
