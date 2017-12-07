import {Record} from 'immutable';

import {TableColumn} from 'app/state/ui/TableColumn';
import {TagNode} from 'app/state/database/TagNode';

class TagTreeTableRecord extends Record({
  columns: [],
  rows: []
}) {}

export class TagTreeTable extends TagTreeTableRecord {
  columns: Array<string>;
  rows: Array<Object>;

  refresh(node: TagNode, path: Array<string>): TagTreeTable {
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

}
