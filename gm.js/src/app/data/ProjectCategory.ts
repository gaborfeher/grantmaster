///<reference path='../../../node_modules/immutable/dist/immutable.d.ts'/>
///<reference path='./IRecord.ts'/>
///<reference path='./BigNumber.ts'/>

import {IRecord} from './IRecord';
import {BigNumber} from './BigNumber';

var Immutable = require('../../../node_modules/immutable/dist/immutable.js');

export interface ProjectCategory extends IRecord<ProjectCategory> {
  tagName: string;
  limitForeign: BigNumber;
  limitPercentageForeign: BigNumber;

  spentForeign: BigNumber;
  spentLocal: BigNumber;

  reset(): ProjectCategory;
  addSpentAmounts(local: BigNumber, foreign: BigNumber);
}
export var ProjectCategory = Immutable.Record({
  tagName: '',
  limitForeign: undefined,
  limitPercentageForeign: undefined,
  spentForeign: new BigNumber(0.0),
  spentLocal: new BigNumber(0.0)
});
ProjectCategory.prototype.reset = function(): ProjectCategory {
  var that: ProjectCategory = this;
  return that.merge({
    spentLocal: new BigNumber(0.0),
    spentForeign: new BigNumber(0.0)
  });
};
ProjectCategory.prototype.addSpentAmounts = function(local: BigNumber, foreign: BigNumber) {
  var that: ProjectCategory = this;
  return that.merge({
    spentLocal: that.spentLocal.plus(local),
    spentForeign: that.spentForeign.plus(foreign)
  });
}

