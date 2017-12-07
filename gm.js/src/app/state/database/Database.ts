///<reference path='../../../../node_modules/immutable/dist/immutable.d.ts'/>

import {BigNumber} from 'app/state/core/BigNumber';
import {Changes} from 'app/state/core/Changes';
import {Immutable, IRecord} from 'app/state/core/IRecord';
import {Currency} from 'app/state/database/Currency';
import {Expense} from 'app/state/database/Expense';
import {Project} from 'app/state/database/Project';
import {Income} from 'app/state/database/Income';
import {TagNode} from 'app/state/database/TagNode';

export interface Database extends IRecord<Database> {
  projects: Immutable.List<Project>;
  budgetCategories: TagNode;
  currencies: Immutable.List<Currency>;
  localCurrency: string;

  addProject(project: Project): Database;
  renameTag(oldName: string, newName: string): Database;

  getExpenseBudgetCategories(): TagNode;
  getIncomeBudgetCategories(): TagNode;
}
export var Database = Immutable.Record({
  projects: Immutable.List([]),
  budgetCategories: new TagNode({name: 'Budget categories'}),
  currencies: Immutable.List([]),
  localCurrency: ''
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
Database.prototype.onChange = function(property: string, changes: Changes): Database {
  let that: Database = this;
  if (property === 'budgetCategories' && changes.tagNodeTreeChange) {
    changes.budgetCategoryTreeChange = true;
  }
  return that;
}
Database.prototype.getExpenseBudgetCategories = function(): TagNode {
  return this.budgetCategories.subTags.get(0);
}
Database.prototype.getIncomeBudgetCategories = function(): TagNode {
  return this.budgetCategories.subTags.get(1);
}
