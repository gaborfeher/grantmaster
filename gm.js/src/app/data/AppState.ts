///<reference path='../../../node_modules/immutable/dist/immutable.d.ts'/>
///<reference path='./Database.ts'/>
///<reference path='./TagTreeTable.ts'/>
///<reference path='../data/Changes.ts'/>

var Immutable = require('../../../node_modules/immutable/dist/immutable.js');

import {Changes} from '../data/Changes';
import {Database} from './Database';
import {IRecord} from './IRecord';
import {Project} from './Project';
import {ProjectCategory} from './ProjectCategory';
import {TableColumn} from './TableColumn';
import {TagNode} from './TagNode';
import {TagTreeTable} from './TagTreeTable';

export interface AppState extends IRecord<AppState> {
  database: Database;
  budgetCategoryTable: TagTreeTable;

  mainMenuSelectedItemId: number;
  categoryColumns: Immutable.List<TableColumn>;
  expenseColumns: Immutable.List<TableColumn>;

  updateTableColumns(): AppState;
  onChange(): AppState;
  getSelectedProjectId(): number;
  getSelectedProject(): Project;
}
export var AppState = Immutable.Record({
  database: new Database(),
  budgetCategoryTable: new TagTreeTable(),

  mainMenuSelectedItemId: -2,
  categoryColumns: Immutable.List(),
  expenseColumns: Immutable.List()
});
AppState.prototype.getSelectedProjectId = function(): number {
  let that: AppState = this;
  if (that.mainMenuSelectedItemId >= 0) {
    return that.mainMenuSelectedItemId;
  } else {
    return undefined;
  }
}
AppState.prototype.getSelectedProject = function(): Project {
  let that: AppState = this;
  if (that.getSelectedProjectId() !== undefined) {
    return that.database.projects.get(that.getSelectedProjectId());
  } else {
    return undefined;
  }
}
AppState.prototype.updateTableColumns = function(): AppState {
  let that: AppState = this;

  function getCategoryList() {
    function toNames(prefix: string) {
      return function(node: TagNode) {
        var x = {key: node.name, value: prefix + node.name};
        return Immutable.List([x]).concat(
          node.subTags.flatMap(node => toNames(prefix + '  ')(node)));
      }
    }
    return toNames('')(that.database.budgetCategories);
  }
  let categoryColumns = Immutable.List([
    new TableColumn({
      key: 'tagName',
      value: 'Name',
      kind: 'dropdown',
      items: getCategoryList()
    }),
    new TableColumn({
      key: 'limitForeign',
      value: 'Limit (F)',
      kind: 'number',
      constraints: ['positive']
    }),
    new TableColumn({
      key: 'limitPercentageForeign',
      value: 'Limit% (F)',
      kind: 'number',
      constraints: ['min:0', 'max:100']
    }),
    new TableColumn({
      key: 'spentForeign',
      value: 'Spent (F)',
      kind: 'number',
      editable: false
    }),
    new TableColumn({
      key: 'spentLocal',
      value: 'Spent (L)',
      kind: 'number',
      editable: false
    }),
  ]);

  function getProjectCategoryList():
    Immutable.List<{key: string, value: string}> {

    let project = that.getSelectedProject();
    if (project === undefined) {
      return Immutable.List([]);
    }
    return project.categories.map(
      (value: ProjectCategory) =>
        ({key: value.tagName, value: value.tagName})).toList();
  }
  let expenseColumns = Immutable.List();
  if (that.mainMenuSelectedItemId >= 0) {
    expenseColumns = Immutable.List([
      new TableColumn({
        key: 'date',
        value: 'Date',
        kind: 'date',
        constraints: ['not_null'],
      }),
      new TableColumn({
        key: 'category',
        value: 'Category',
        kind: 'dropdown',
        items: getProjectCategoryList()
      }),
      new TableColumn({
        key: 'accountNo',
        value: 'Account Number',
        kind: 'string'
      }),
      new TableColumn({
        key: 'partner',
        value: 'Partner',
        kind: 'string'
      }),
      new TableColumn({
        key: 'localAmount',
        value: 'Local amount',
        kind: 'number',
        constraints: ['positive']
      }),
      new TableColumn({
        key: 'foreignAmount',
        value: 'Foreign amount',
        kind: 'number',
        editable: false
      }),
      new TableColumn({
        key: 'exchangeRate',
        value: 'Exchange rate',
        kind: 'number',
        editable: false
      })
    ]);
  }

  return that
    .set('categoryColumns', categoryColumns)
    .set('expenseColumns', expenseColumns);
}
AppState.prototype.onChange = function(property: string, changes: Changes): AppState {
  let that: AppState = this;
  if (changes.budgetCategoryChange
      || changes.budgetCategoryTreeChange
      || changes.significantExpenseChange) {
    return that
      .set(
        'budgetCategoryTable',
        that.budgetCategoryTable.refresh(
          that.database.budgetCategories,
          ['database', 'budgetCategories']))
      .updateTableColumns();
  } else if (property === 'mainMenuSelectedItemId' || changes.projectCategoryListChange) {
    return that.updateTableColumns();
  } else {
    return that;
  }
}
