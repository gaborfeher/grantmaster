import {List, OrderedMap, Record} from 'immutable';
import {BigNumber} from 'bignumber.js';

import {Changes} from 'app/state/core/Changes';

class TagNodeRecord extends Record({
  name: undefined,
  subTags: List(),
  summaries: OrderedMap()  // only used in copies of nodes inside UI state
}) {}

export class TagNode extends TagNodeRecord {
  name: string;
  subTags: List<TagNode>;

  summaries: OrderedMap<string, BigNumber>;

  onChange(property: string, changes: Changes): TagNode {
    // TODO: filter out summary changes?
    changes.tagNodeTreeChange = true;
    return this;
  }

  getSubTreeAsUIList(): List<{key: string, value: string}> {
    let root: TagNode = this;
    function toNames(prefix: string) {
      return function(node: TagNode): List<{key: string, value: string}>  {
        let x = {key: node.name, value: prefix + node.name};
        return List([x]).concat(
          node.subTags.flatMap(node => toNames(prefix + '  ')(node)));
      }
    }
    return toNames('')(root);
  }
}

