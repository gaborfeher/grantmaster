///<reference path='../core/IRecord.ts'/>

import {Immutable, IRecord} from '../core/IRecord';

export interface Currency extends IRecord<Currency> {
  name: string;
}
export var Currency = Immutable.Record({
  name: ''
});
