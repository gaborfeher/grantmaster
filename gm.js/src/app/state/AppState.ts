///<reference path='../../../node_modules/immutable/dist/immutable.d.ts'/>

import {BigNumber} from 'app/state/core/BigNumber';
import {Changes} from 'app/state/core/Changes';
import {Currency} from 'app/state/database/Currency';
import {Database} from 'app/state/database/Database';
import {Expense} from 'app/state/database/Expense';
import {Income} from 'app/state/database/Income';
import {Immutable, IRecord} from 'app/state/core/IRecord';
import {Project} from 'app/state/database/Project';
import {ProjectCategory} from 'app/state/database/ProjectCategory';
import {TableColumn} from 'app/state/ui/TableColumn';
import {TagNode} from 'app/state/database/TagNode';
import {GenericTable} from 'app/state/ui/GenericTable';
import {TagTreeTable} from 'app/state/ui/TagTreeTable';

export interface AppState extends IRecord<AppState> {
  database: Database;
  budgetCategoryTable: TagTreeTable;
  currencyTable: GenericTable<Currency>;
  projectUIState: Immutable.Record.Class;

  mainMenuSelectedItemId: number;

  updateBudgetCategories(): AppState;
  resetNewItems(): AppState;
  onChange(): AppState;
  getSelectedProjectId(): number;
  getSelectedProject(): Project;
}
export var AppState = Immutable.Record({
  database: new Database(),
  budgetCategoryTable: new TagTreeTable(),

  mainMenuSelectedItemId: -3,

  // TODO: move this into own class
  projectUIState: new (Immutable.Record({
    expenseTable: new GenericTable({
      myPath: ['projectUIState', 'expenseTable'],
      newItemTemplate: new Expense()
    }),
    incomeTable: new GenericTable({
      myPath: ['projectUIState', 'incomeTable'],
      newItemTemplate: new Income()
    }),
    categoryTable: new GenericTable({
      myPath: ['projectUIState', 'categoryTable'],
      newItemTemplate: new ProjectCategory()
    }),
  })),

  currencyTable: new GenericTable({
    myPath: ['currencyTable'],
    newItemTemplate: new Currency(),
    columns: Immutable.List([
      new TableColumn({
        key: 'name',
        value: 'Name',
        kind: 'string'
      })
    ])
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
AppState.prototype.resetNewItems = function(): AppState {
  let that: AppState = this;
  return that
    .updateIn(['currencyTable'], table => table.resetNewItem())
    .updateIn(['projectUIState', 'incomeTable'], table => table.resetNewItem())
    .updateIn(['projectUIState', 'categoryTable'], table => table.resetNewItem())
    .updateIn(['projectUIState', 'expenseTable'], table => table.resetNewItem());
}


function sumBudgetCategories(
    node: TagNode, map: Object, rootNode: boolean): TagNode {
  node = node.set(
    'subTags',
    node.subTags.map(subTag => sumBudgetCategories(subTag, map, false)));
  let items = {};  // year -> value (sum for this node)

  // Take out income/expense values directly assigned to this node 
  // from map.
  if (node.name in map) {
    let localItems = map[node.name];
    for (var key in localItems) {
      if (localItems.hasOwnProperty(key)) {
        let year = key.split(':')[1];
        items[year] = localItems[key];
      }
    }
  }

  // Add values from subtree into items.
  let firstChild = true;
  node.subTags.forEach(
    subTag => {
      let multiplier = 1.0;
      if (firstChild && rootNode) multiplier = -1.0;  // Expenses.
      firstChild = false;
      subTag.summaries.forEach(
        (summaryItem, year) => {
          items[year] = items[year] || new BigNumber(0);
          items[year] = items[year].plus(summaryItem.times(multiplier));
          return true;
        }
      );
      return true;
    });


  let orderedItems = Immutable.OrderedMap();
  for (var year in items) {
    if (items.hasOwnProperty(year)) {
      orderedItems = orderedItems.set(year, items[year]);
    }
  }
  node = node.set('summaries', orderedItems);
  return node;
}
AppState.prototype.updateBudgetCategories = function() {
  let that: AppState = this;

  var map = {};  // map[category name][year] = sum
  that.database.projects.forEach(
    project => {
      project.expenses.forEach(
        expense => {
          let year = expense.date.split('-')[0];
          let amount = expense.localAmount;
          let category = expense.category;
          let key = category + ':' + year;
          map[category] = map[category] || {};
          let baseAmount = map[category][key] || new BigNumber(0);
          map[category][key] = baseAmount.plus(amount);
          return true;
        });
      if (project.incomeCategory != '') {
        project.incomes.forEach(
          income => {
            let year = income.date.split('-')[0];
            let amount = income.localAmount;
            let category = project.incomeCategory;
            let key = category + ':' + year;
            map[category] = map[category] || {};
            let baseAmount = map[category][key] || new BigNumber(0);
            map[category][key] = baseAmount.plus(amount);
            return true;
          });
      }

      return true;
    });

  let budgetCategoriesWithSums =
    sumBudgetCategories(that.database.budgetCategories, map, true);
  return that.set(
    'budgetCategoryTable',
    that.budgetCategoryTable.refresh(
      budgetCategoriesWithSums,
      ['database', 'budgetCategories']))
}
AppState.prototype.onChange = function(property: string, changes: Changes): AppState {
  let that: AppState = this;
  if (property === 'mainMenuSelectedItemId') {
    that = that.resetNewItems();
  }
  if (changes.projectProperty === 'expenses'
      || changes.projectProperty === 'incomes'
      || changes.projectProperty === 'incomeCategory'
      || changes.budgetCategoryTreeChange) {
    return that.updateBudgetCategories();
  } else {
    return that;
  }
}
