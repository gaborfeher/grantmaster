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
  times(other: BigNumber|number): BigNumber;
  lessThan(other: BigNumber): boolean;
  lessThanOrEqualTo(other: number): boolean;
  greaterThan(other: BigNumber): boolean;
  greaterThanOrEqualTo(other: BigNumber): boolean;
  round(dp: number, rm: number): BigNumber;
  toFormat(): string;
}
export function bigMin(a: BigNumber, b: BigNumber): BigNumber {
  return a.lessThan(b) ? a : b;
}
export function bigFormat(num: BigNumber): string {
  let s = num.round(3, BigNumber.HALF_UP).toFormat();
  let dotPos = s.indexOf('.');
  if (dotPos < 0) {
    s += '.';
    dotPos = s.length - 1;
  }
  let numDecimals = s.length - dotPos - 1;
  for (let i = numDecimals; i < 3; ++i) {
    s += '0';
  }
  return s;
}
