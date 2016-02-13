///<reference path='../../../../node_modules/immutable/dist/immutable.d.ts'/>
///<reference path='../core/BigNumber.ts'/>
///<reference path='../core/Changes.ts'/>
///<reference path='../core/IRecord.ts'/>
///<reference path='./Expense.ts'/>
///<reference path='./Project.ts'/>
///<reference path='./Income.ts'/>
///<reference path='./TagNode.ts'/>

var Immutable = require('../../../../node_modules/immutable/dist/immutable.js');

import {BigNumber} from '../core/BigNumber';
import {Changes} from '../core/Changes';
import {IRecord} from '../core/IRecord';
import {Currency} from './Currency';
import {Expense} from './Expense';
import {Project} from './Project';
import {Income} from './Income';
import {TagNode} from './TagNode';


export interface Database extends IRecord<Database> {
  projects: Immutable.List<Project>;
  budgetCategories: TagNode;
  currencies: Immutable.List<Currency>;
  localCurrency: string;

  addProject(project: Project): Database;
  renameTag(oldName: string, newName: string): Database;
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
