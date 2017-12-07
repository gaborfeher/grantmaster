import {BigNumber} from 'app/state/core/BigNumber';
import {Changes} from 'app/state/core/Changes';
import {Immutable, IRecord} from 'app/state/core/IRecord';
import {Utils} from 'app/utils/Utils';

export interface Expense extends IRecord<Expense> {
  date: string;
  localAmount: BigNumber;
  foreignAmount: BigNumber;
  exchangeRate: BigNumber;
  category: string;

  multiPart: boolean;
  overshoot: boolean;

  resetComputed(): Expense;
  validate(): String[];
}
export var Expense = Immutable.Record({
  date: undefined,
  localAmount: undefined,
  foreignAmount: undefined,
  exchangeRate: undefined,
  accountNo: undefined,
  partner: undefined,
  category: undefined,

  multiPart: false,
  overshoot: false
});
Expense.prototype.resetComputed = function(): Expense {
  let that: Expense = this;
  return that.merge({
    foreignAmount: new BigNumber(0),
    exchangeRate: new BigNumber(0),
    multiPart: false,
    overshoot: false
  });
}
Expense.prototype.validate = function(): String[] {
  let errors = [];
  if (!this.date || !Utils.validateDate(this.date)) {
    errors.push('invalid date');
  }
  if (!this.localAmount || this.localAmount.lessThanOrEqualTo(0.0)) {
    errors.push('non-positive local amount');
  }
  return errors;
}
export function compareExpenses(a: Expense, b: Expense): number {
  if (a.date == b.date) {
    return 0;
  } else if (a.date > b.date) {
    return 1;
  } else {
    return -1;
  }
}

