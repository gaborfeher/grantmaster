///<reference path='../core/IRecord.ts'/>

import {IRecord} from '../core/IRecord';

var Immutable = require('../../../../node_modules/immutable/dist/immutable.js');

export interface Currency extends IRecord<Currency> {
  name: string;
}
export var Currency = Immutable.Record({
  name: ''
});
