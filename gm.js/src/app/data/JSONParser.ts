///<reference path='./Model.ts'/>

import {Injectable} from 'angular2/core';
import {Database, Expense, Income, Project, ProjectCategory, TagNode} from './Model';

var BigNumber = require('../../../node_modules/bignumber.js/bignumber.js');

@Injectable()
export class JSONParser {

  parseDatabase(jsonData) {
    var that = this;
    return new Database({
      projects: that.parseList(jsonData.projects, project => that.parseProject(project)),
      budgetCategories: that.parseTagNode(jsonData.budgetCategories)
    }).recomputeBudgetCategories();
  }

  parseList<F, T>(list: Array<F>, mapper: (F) => T ): Immutable.List<T> {
    let list2 = [];
    for (var i = 0; i < list.length; ++i) {
      list2.push(mapper(list[i]));
    }
    return Immutable.List(list2);
  }

  parseProject(json: any): Project {
    let that = this;
    let project = new Project({
      name: json.name,
      incomes: that.parseList(json.incomes, income => that.parseIncome(income)),
      expenses: that.parseList(json.expenses, expense => that.parseExpense(expense)),
      categories: that.parseList(json.categories, category => that.parseCategory(category)),
    });
    return project
      .recomputeIncomes()
      .recomputeBudgetCategories();
  }

  parseExpense(expense: any): Expense {
    return new Expense({
      date: expense.date,
      localAmount: new BigNumber(expense.localAmount),
      accountNo: expense.accountNo,
      partner: expense.partner,
      category: expense.category
    });
  }

  parseIncome(income: any): Income {
    return new Income({
      date: income.date,
      foreignAmount: new BigNumber(income.foreignAmount),
      exchangeRate: new BigNumber(income.exchangeRate)
    });
  }

  parseCategory(category: any): ProjectCategory {
    var that = this;
    return new ProjectCategory({
      tagName: category.tagName,
      limitForeign: category.limitForeign,
      limitPercentageForeign: category.limitPercentageForeign
    });
  }

  parseTagNode(node: any): TagNode {
    var that = this;
    return new TagNode({
      name: node.name,
      subTags: that.parseList(node.subTags, node => that.parseTagNode(node)),
    });
  }

}
