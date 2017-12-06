///<reference path='../../../../node_modules/immutable/dist/immutable.d.ts'/>
///<reference path='../core/BigNumber.ts'/>
///<reference path='../core/Changes.ts'/>
///<reference path='../core/IRecord.ts'/>

import {BigNumber} from '../core/BigNumber';
import {Changes} from '../core/Changes';
import {Immutable, IRecord} from '../core/IRecord';
import {Utils} from '../../utils/Utils';

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

  validate(): String[];
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
Income.prototype.validate = function(): String[] {
  let that: Income = this;
  let errors = [];
  if (!that.date || !Utils.validateDate(that.date)) {
    errors.push('invalid date');
  }
  if (!that.foreignAmount || that.foreignAmount.lessThanOrEqualTo(0.0)) {
    errors.push('non-positive foreign amount');
  }
  console.log('income.validata: ', errors);
  return errors;
}
export function compareIncomes(a: Income, b: Income): number {
  if (a.date == b.date) {
    return 0;
  } else if (a.date > b.date) {
    return 1;
  } else {
    return -1;
  }
}

