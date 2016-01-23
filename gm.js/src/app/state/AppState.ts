///<reference path='../../../node_modules/immutable/dist/immutable.d.ts'/>
///<reference path='./database/Database.ts'/>
///<reference path='./database/ProjectCategory.ts'/>
///<reference path='./database/Expense.ts'/>
///<reference path='./database/Income.ts'/>
///<reference path='./ui/GenericTable.ts'/>
///<reference path='./ui/TagTreeTable.ts'/>
///<reference path='./ui/TableColumn.ts'/>
///<reference path='./core/Changes.ts'/>

var Immutable = require('../../../node_modules/immutable/dist/immutable.js');

import {Changes} from './core/Changes';
import {Database} from './database/Database';
import {Expense} from './database/Expense';
import {Income} from './database/Income';
import {IRecord} from './core/IRecord';
import {Project} from './database/Project';
import {ProjectCategory} from './database/ProjectCategory';
import {TableColumn} from './ui/TableColumn';
import {TagNode} from './database/TagNode';
import {GenericTable} from './ui/GenericTable';
import {TagTreeTable} from './ui/TagTreeTable';

export interface AppState extends IRecord<AppState> {
  database: Database;
  budgetCategoryTable: TagTreeTable;

  expenseTable: GenericTable<Expense>;
  incomeTable: GenericTable<Income>;
  categoryTable: GenericTable<ProjectCategory>;

  mainMenuSelectedItemId: number;

  updateTableColumns(): AppState;
  resetNewItems(): AppState;
  onChange(): AppState;
  getSelectedProjectId(): number;
  getSelectedProject(): Project;
}
export var AppState = Immutable.Record({
  database: new Database(),
  budgetCategoryTable: new TagTreeTable(),

  mainMenuSelectedItemId: -2,
  categoryColumns: Immutable.List(),
  expenseColumns: Immutable.List(),

  expenseTable: new GenericTable({
    myPath: ['expenseTable'],
    newItemTemplate: new Expense()
  }),
  incomeTable: new GenericTable({
    myPath: ['incomeTable'],
    newItemTemplate: new Income()
  }),
  categoryTable: new GenericTable({
    myPath: ['categoryTable'],
    newItemTemplate: new ProjectCategory()
  })
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

  let incomeColumns = Immutable.List([
    new TableColumn({
      key: 'date',
      value: 'Date',
      kind: 'date',
      constraints: ['not_null'],
    }),
    new TableColumn({
      key: 'foreignAmount',
      value: 'Foreign amount',
      kind: 'number',
      constraints: ['not_null', 'positive']
    }),
    new TableColumn({
      key: 'exchangeRate',
      value: 'Exchange rate',
      kind: 'number',
      constraints: ['not_null', 'positive']
    }),
    new TableColumn({
      key: 'localAmount',
      value: 'Local amount',
      kind: 'number',
      editable: false
    }),
    new TableColumn({
      key: 'spentForeignAmount',
      value: 'Spent (F)',
      kind: 'number',
      editable: false
    }),
    new TableColumn({
      key: 'spentLocalAmount',
      value: 'Spent (L)',
      kind: 'number',
      editable : false
    })
  ]);

  return that
    .setIn(['incomeTable', 'columns'], incomeColumns)
    .setIn(['categoryTable', 'columns'], categoryColumns)
    .setIn(['expenseTable', 'columns'], expenseColumns);
}
AppState.prototype.resetNewItems = function(): AppState {
  let that: AppState = this;
  return that
    .updateIn(['incomeTable'], table => table.resetNewItem())
    .updateIn(['categoryTable'], table => table.resetNewItem())
    .updateIn(['expenseTable'], table => table.resetNewItem());
}
AppState.prototype.onChange = function(property: string, changes: Changes): AppState {
  let that: AppState = this;
  if (property === 'mainMenuSelectedItemId') {
    that = that.resetNewItems();
  }
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
