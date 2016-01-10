///<reference path='../../../node_modules/immutable/dist/immutable.d.ts'/>
///<reference path='./Changes.ts'/>
///<reference path='./Expense.ts'/>
///<reference path='./IRecord.ts'/>
///<reference path='./Project.ts'/>
///<reference path='./Income.ts'/>
///<reference path='./TagNode.ts'/>

var Immutable = require('../../../node_modules/immutable/dist/immutable.js');
var BigNumber = require('../../../node_modules/bignumber.js/bignumber.js');

import {Changes} from './Changes';
import {Expense} from './Expense';
import {IRecord} from './IRecord';
import {Project} from './Project';
import {Income} from './Income';
import {TagNode} from './TagNode';


export interface Database extends IRecord<Database> {
  projects: Immutable.List<Project>;
  budgetCategories: TagNode;

  addProject(project: Project): Database;
  renameTag(oldName: string, newName: string): Database;
  recomputeBudgetCategories(): Database;
}
export var Database = Immutable.Record({
  projects: Immutable.List([]),
  budgetCategories: new TagNode({name: 'Budget categories'}),
  programs: Immutable.Map({}),
  currencies: Immutable.Map({}),
});
Database.prototype.addProject = function(project: Project): Database {
  var that: Database = this;
  return that.set('projects', that.projects.push(project));
};
Database.prototype.renameTag = function(oldName: string, newName: string) {
  var that: Database = this;
  return that.set(
    'projects',
    that.projects.map(
      project => project.merge({
        categories: project.categories.map(
          category => {
            if (category.tagName === oldName) {
              return category.set('tagName', newName);
            } else {
              return category;
            }
          }),
        expenses: project.expenses.map(
          expense => {
            if (expense.category === oldName) {
              return expense.set('category', newName);
            } else {
              return expense;
            }
          }
        )
      })));
}
function mapBudgetCategories(node: TagNode, map: Object): TagNode {
  node = node.set('subTags', node.subTags.map(subTag => mapBudgetCategories(subTag, map)));
  let items = {};
  if (node.name in map) {
    let localItems = map[node.name];
    for (var key in localItems) {
      if (localItems.hasOwnProperty(key)) {
        let year = key.split(':')[1];
        items[year] = localItems[key];
      }
    }
  }

  node.subTags.forEach(
    subTag => {
      subTag.summaries.forEach(
        (summaryItem, year) => {
          items[year] = items[year] || new BigNumber(0);
          items[year] = items[year].plus(summaryItem);
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
Database.prototype.recomputeBudgetCategories = function() {
  let that: Database = this;
  var map = {};
  that.projects.forEach(
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
      return true;
    });
  return that.set('budgetCategories', mapBudgetCategories(that.budgetCategories, map));
}
Database.prototype.onChange = function(changes: Changes) {
  let that: Database = this;
  if (changes.significantExpenseChange || changes.budgetCategoryChange) {
    return that.recomputeBudgetCategories();
  } else {
    return that;
  }
}
