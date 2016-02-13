///<reference path='../../../../node_modules/immutable/dist/immutable.d.ts'/>
///<reference path='../core/IRecord.ts'/>
///<reference path='../core/BigNumber.ts'/>
///<reference path='../core/Changes.ts'/>

import {IRecord} from '../core/IRecord';
import {BigNumber} from '../core/BigNumber';
import {Changes} from '../core/Changes';

var Immutable = require('../../../../node_modules/immutable/dist/immutable.js');

export interface TagNode extends IRecord<TagNode> {
  name: string;
  subTags: Immutable.List<TagNode>;

  summaries: Immutable.OrderedMap<string, BigNumber>;
}
export var TagNode = Immutable.Record({
  name: undefined,
  subTags: Immutable.List(),
  summaries: Immutable.OrderedMap()  // only used in copies of nodes inside UI state
});
TagNode.prototype.onChange = function(property: string, changes: Changes): TagNode {
  // TODO: filter out summary changes?
  changes.tagNodeTreeChange = true;
  return this;
}
