///<reference path='../../../../node_modules/immutable/dist/immutable.d.ts'/>

var Immutable = require('../../../../node_modules/immutable/dist/immutable.js');

import {IRecord} from '../core/IRecord';

export interface TableColumn extends IRecord<TableColumn> {
  key: string;
  value: string;
  kind: string;
  items: Immutable.List<string>;
  constraints: Immutable.List<string>;
  editable: boolean;
}
export var TableColumn = Immutable.Record({
  key: '',
  value: '',
  kind: '',
  items: Immutable.List(),
  constraints: Immutable.List(),
  editable: true
});
