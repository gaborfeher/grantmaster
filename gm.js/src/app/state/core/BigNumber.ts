declare var require: any;
export var BigNumber = require('../../../../node_modules/bignumber.js/bignumber.js');

BigNumber.config({
  DECIMAL_PLACES: 30,
  ROUNDING_MODE: BigNumber.ROUND_HALF_EVEN,
  FORMAT: {
    groupSeparator: '',
    decimalSeparator: '.'
  }
});

export interface BigNumber {
  minus(other: BigNumber): BigNumber;
  plus(other: BigNumber): BigNumber;
  dividedBy(other: BigNumber): BigNumber;
  times(other: BigNumber): BigNumber;
  lessThan(other: BigNumber): boolean;
  greaterThanOrEqualTo(other: BigNumber): boolean;
  round(dp: number, rm: number): BigNumber;
  toFormat(): string;
}
export function bigMin(a: BigNumber, b: BigNumber): BigNumber {
  return a.lessThan(b) ? a : b;
}
