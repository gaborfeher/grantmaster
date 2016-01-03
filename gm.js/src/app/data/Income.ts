///<reference path='../../../node_modules/immutable/dist/immutable.d.ts'/>
///<reference path='./IRecord.ts'/>
///<reference path='./BigNumber.ts'/>

import {IRecord} from './IRecord';
import {BigNumber} from './BigNumber';

var Immutable = require('../../../node_modules/immutable/dist/immutable.js');

export interface Income extends IRecord<Income> {
  date: string;
  foreignAmount: BigNumber;
  exchangeRate: BigNumber;
  localAmount: BigNumber;
  spentForeignAmount: BigNumber;
  spentLocalAmount: BigNumber;

  spendInLocalCurrency(localAmount: BigNumber): Income;
  refresh(): Income;
  resetComputed(): Income;
}
export var Income = Immutable.Record({
  date: undefined,
  foreignAmount: undefined,
  exchangeRate: undefined,
  localAmount: undefined,
  spentForeignAmount: undefined,
  spentLocalAmount: undefined
});
Income.prototype.refresh = function(): Income {
  let that: Income = this;
  return that.merge({
    localAmount: that.foreignAmount.times(that.exchangeRate)
  });
};
Income.prototype.resetComputed = function(): Income {
  let that: Income = this;
  return that.merge({
    spentForeignAmount: new BigNumber(0),
    spentLocalAmount: new BigNumber(0)
  });
};
Income.prototype.spendInLocalCurrency = function(localAmount: BigNumber): Income {
  let that: Income = this;
  return that.merge({
    spentLocalAmount: that.spentLocalAmount.plus(localAmount),
    spentForeignAmount: that.spentForeignAmount.plus(localAmount.dividedBy(that.exchangeRate))
  });
};
export function compareIncomes(a: Income, b: Income): number {
  if (a.date == b.date) {
    return 0;
  } else if (a.date > b.date) {
    return 1;
  } else {
    return -1;
  }
}

