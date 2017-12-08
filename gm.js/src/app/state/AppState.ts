import {List, OrderedMap, Record} from 'immutable';

import {BigNumber} from 'app/state/core/BigNumber';
import {Changes} from 'app/state/core/Changes';
import {Currency} from 'app/state/database/Currency';
import {Database} from 'app/state/database/Database';
import {Expense} from 'app/state/database/Expense';
import {Income} from 'app/state/database/Income';
import {Project} from 'app/state/database/Project';
import {ProjectCategory} from 'app/state/database/ProjectCategory';
import {TableColumn} from 'app/state/ui/TableColumn';
import {TagNode} from 'app/state/database/TagNode';
import {GenericTable} from 'app/state/ui/GenericTable';
import {TagTreeTable} from 'app/state/ui/TagTreeTable';

// TODO: move this into own class in own file
class ProjectUIState extends Record({
  expenseTable: new GenericTable<Expense>({
    myPath: ['projectUIState', 'expenseTable'],
    newItemTemplate: new Expense()
  }),
  incomeTable: new GenericTable<Income>({
    myPath: ['projectUIState', 'incomeTable'],
    newItemTemplate: new Income()
  }),
  categoryTable: new GenericTable<ProjectCategory>({
    myPath: ['projectUIState', 'categoryTable'],
    newItemTemplate: new ProjectCategory()
  }),
}) {}

class AppStateRecord extends Record({
  database: new Database(),
  budgetCategoryTable: new TagTreeTable(),

  mainMenuSelectedItemId: -3,

  projectUIState: new ProjectUIState(),

  currencyTable: new GenericTable<Currency>({
    myPath: ['currencyTable'],
    newItemTemplate: new Currency(),
    columns: List([
        new TableColumn({
          key: 'name',
        value: 'Name',
        kind: 'string'
      })
    ])
  })
}) {}

export class AppState extends AppStateRecord {
  database: Database;
  budgetCategoryTable: TagTreeTable;
  currencyTable: GenericTable<Currency>;
  projectUIState: ProjectUIState;

  mainMenuSelectedItemId: number;

  updateBudgetCategories(): AppState {
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

  resetNewItems(): AppState {
    let that: AppState = this;
    return that
      .updateIn(['currencyTable'], table => table.resetNewItem())
      .updateIn(['projectUIState', 'incomeTable'], table => table.resetNewItem())
      .updateIn(['projectUIState', 'categoryTable'], table => table.resetNewItem())
      .updateIn(['projectUIState', 'expenseTable'], table => table.resetNewItem());
  }

  onChange(property: string, changes: Changes): AppState {
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

  getSelectedProjectId(): number {
    let that: AppState = this;
    if (that.mainMenuSelectedItemId >= 0) {
      return that.mainMenuSelectedItemId;
    } else {
      return undefined;
    }
  }

  getSelectedProject(): Project {
    let that: AppState = this;
    if (that.getSelectedProjectId() !== undefined) {
      return that.database.projects.get(that.getSelectedProjectId());
    } else {
      return undefined;
    }
  }
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


  let orderedItems = OrderedMap();
  for (var year in items) {
    if (items.hasOwnProperty(year)) {
      orderedItems = orderedItems.set(year, items[year]);
    }
  }
  node = node.set('summaries', orderedItems);
  return node;
}

