///<reference path='../../../node_modules/immutable/dist/immutable.d.ts'/>
///<reference path='./IRecord.ts'/>
///<reference path='./BigNumber.ts'/>

import {IRecord} from './IRecord';
import {BigNumber} from './BigNumber';

var Immutable = require('../../../node_modules/immutable/dist/immutable.js');

export interface TagNode extends IRecord<TagNode> {
  name: string;
  subTags: Immutable.List<TagNode>;

  summaries: Immutable.OrderedMap<string, BigNumber>;
}
export var TagNode = Immutable.Record({
  name: undefined,
  subTags: Immutable.List(),
  summaries: Immutable.OrderedMap()
});

