///<reference path='../../../node_modules/immutable/dist/immutable.d.ts'/>
///<reference path='./IRecord.ts'/>
///<reference path='./BigNumber.ts'/>
///<reference path='./Changes.ts'/>

import {IRecord} from './IRecord';
import {BigNumber} from './BigNumber';
import {Changes} from './Changes';

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
TagNode.prototype.onChange = function(property: string, changes: Changes): TagNode {
  // TODO: filter out summary changes?
  changes.tagNodeTreeChange = true;
  return this;
}
