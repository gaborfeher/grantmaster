///<reference path='../../../../node_modules/immutable/dist/immutable.d.ts'/>

import {Immutable, IRecord} from 'app/state/core/IRecord';
import {BigNumber} from 'app/state/core/BigNumber';
import {Changes} from 'app/state/core/Changes';

export interface TagNode extends IRecord<TagNode> {
  name: string;
  subTags: Immutable.List<TagNode>;

  summaries: Immutable.OrderedMap<string, BigNumber>;

  getSubTreeAsUIList();
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
TagNode.prototype.getSubTreeAsUIList = function() {
  let root: TagNode = this;
  function toNames(prefix: string) {
    return function(node: TagNode) {
      let x = {key: node.name, value: prefix + node.name};
      return Immutable.List([x]).concat(
        node.subTags.flatMap(node => toNames(prefix + '  ')(node)));
    }
  }
  return toNames('')(root);
}
