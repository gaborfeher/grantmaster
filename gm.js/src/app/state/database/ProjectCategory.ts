///<reference path='../core/IRecord.ts'/>
///<reference path='../core/BigNumber.ts'/>
///<reference path='../core/Changes.ts'/>

import {IRecord} from '../core/IRecord';
import {BigNumber} from '../core/BigNumber';
import {Changes} from '../core/Changes';

var Immutable = require('../../../../node_modules/immutable/dist/immutable.js');

export interface ProjectCategory extends IRecord<ProjectCategory> {
  tagName: string;
  limitForeign: BigNumber;
  limitPercentageForeign: BigNumber;

  spentForeign: BigNumber;
  spentLocal: BigNumber;

  overshoot: boolean;

  reset(): ProjectCategory;
  addSpentAmounts(local: BigNumber, foreign: BigNumber);
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

