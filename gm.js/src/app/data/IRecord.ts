///<reference path='../../../node_modules/immutable/dist/immutable.d.ts'/>

var Immutable = require('../../../node_modules/immutable/dist/immutable.js');

export interface IRecord<T> extends Immutable.Record.Class {
  // Scavenged from:
  // https://github.com/facebook/immutable-js/blob/master/type-definitions/Immutable.d.ts

  constructor(vals: any);

  getIn(searchKeyPath: Array<any>, notSetValue?: any): any;
  getIn(searchKeyPath: Immutable.Iterable<any, any>, notSetValue?: any): any;

  setIn(keyPath: Array<any>, value: any): T;
  setIn(keyPath: Immutable.Iterable<any, any>, value: any): T;
  set(key: string, value: any): T;

  updateIn(
    keyPath: Array<any>,
    updater: (value: any) => any
    ): T;

  merge(mergeValues: any): T;

  toObject(): any;
  toJS(): any;
  toJSON(): any;
}
