///<reference path='../../../../node_modules/immutable/dist/immutable.d.ts'/>

import {Immutable, IRecord} from 'app/state/core/IRecord';
import {TableColumn} from 'app/state/ui/TableColumn';
import {TagNode} from 'app/state/database/TagNode';

export interface TagTreeTable extends IRecord<TagTreeTable> {
  columns: Array<string>,
  rows: Array<Object>,
  refresh(node: TagNode, path: Array<string>): TagTreeTable;
}
export var TagTreeTable = Immutable.Record({
  columns: [],
  rows: []
});
TagTreeTable.prototype.refresh = function(node: TagNode, path: Array<string>): TagTreeTable {
  let columns = node.summaries.keySeq()
    .map(columnName => new TableColumn({key: columnName, kind: 'number', editable: false}))
    .toArray();
  function getGlobalCategoryList(node: TagNode, path: Array<string | number>, list: Array<Object>) {
    list.push({node: node, path: path});
    node.subTags.forEach(
      (subTag, index) => {
        getGlobalCategoryList(subTag, path.concat(['subTags', index]), list);
        return true;
      });

    return list;
  }
  let rows = getGlobalCategoryList(node, path, []);
  return new TagTreeTable({
    columns: columns,
    rows: rows
  });
}
