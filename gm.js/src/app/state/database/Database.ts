import {Record, List} from 'immutable';

import {BigNumber} from 'app/state/core/BigNumber';
import {Changes} from 'app/state/core/Changes';
import {Currency} from 'app/state/database/Currency';
import {Expense} from 'app/state/database/Expense';
import {Project} from 'app/state/database/Project';
import {Income} from 'app/state/database/Income';
import {TagNode} from 'app/state/database/TagNode';


class DatabaseRecord extends Record({
  projects: List([]),
  budgetCategories: new TagNode({name: 'Budget categories'}),
  currencies: List([]),
  localCurrency: '',
  nextUniqueId: 0,
}) {}

export class Database extends DatabaseRecord {
  projects: List<Project>;
  budgetCategories: TagNode;
  currencies: List<Currency>;
  localCurrency: string;
  nextUniqueId: number;  // TODO

  addProject(project: Project): Database {
    return this.set('projects', this.projects.push(project));
  }

  renameTag(oldName: string, newName: string): Database {
    let that: Database = this;
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

  getExpenseBudgetCategories(): TagNode {
    return this.budgetCategories.subTags.get(0);
  }

  getIncomeBudgetCategories(): TagNode {
    return this.budgetCategories.subTags.get(1);
  }

  onChange(property: string, changes: Changes): Database {
    let that: Database = this;
    if (property === 'budgetCategories' && changes.tagNodeTreeChange) {
      changes.budgetCategoryTreeChange = true;
    }
    return that;
  }

  incrementNextUniqueId(): Database {
    return this.set('nextUniqueId', this.nextUniqueId + 1);
  }

}
