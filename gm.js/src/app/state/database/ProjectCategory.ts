import {BigNumber} from 'app/state/core/BigNumber';
import {Changes} from 'app/state/core/Changes';
import {Immutable, IRecord} from 'app/state/core/IRecord';

export interface ProjectCategory extends IRecord<ProjectCategory> {
  tagName: string;
  limitForeign: BigNumber;
  limitPercentageForeign: BigNumber;

  spentForeign: BigNumber;
  spentLocal: BigNumber;

  overshoot: boolean;

  reset(): ProjectCategory;
  addSpentAmounts(local: BigNumber, foreign: BigNumber);
  validate(): String[];
}
export var ProjectCategory = Immutable.Record({
  tagName: '',
  limitForeign: undefined,
  limitPercentageForeign: undefined,
  spentForeign: new BigNumber(0.0),
  spentLocal: new BigNumber(0.0),
  overshoot: false
});
ProjectCategory.prototype.reset = function(): ProjectCategory {
  var that: ProjectCategory = this;
  return that.merge({
    spentLocal: new BigNumber(0.0),
    spentForeign: new BigNumber(0.0),
    overshoot: false
  });
};
ProjectCategory.prototype.addSpentAmounts = function(local: BigNumber, foreign: BigNumber) {
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
ProjectCategory.prototype.validate = function(): String[] {
  let errors = [];
  // TODO: what to validate?
  return errors;
}

