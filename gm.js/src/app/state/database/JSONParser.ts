///<reference path='./Database.ts'/>
///<reference path='./Project.ts'/>
///<reference path='./ProjectCategory.ts'/>
///<reference path='./Expense.ts'/>
///<reference path='./Income.ts'/>
///<reference path='./TagNode.ts'/>

import {Injectable} from 'angular2/core';

import {Currency} from './Currency';
import {Database} from './Database';
import {Expense} from './Expense';
import {Income} from './Income';
import {Project} from './Project';
import {ProjectCategory} from './ProjectCategory';
import {TagNode} from './TagNode';

import {BigNumber} from '../core/BigNumber';

@Injectable()
export class JSONParser {

  parseDatabase(jsonData) {
    var that = this;
    return new Database({
      projects: that.parseList(
        jsonData.projects,
        project => that.parseProject(project)),
      budgetCategories: that.parseTagNode(jsonData.budgetCategories),
      currencies: that.parseList(
        jsonData.currencies,
        currency => that.parseCurrency(currency)),
      localCurrency: jsonData.localCurrency
    });
  }

  parseList<F, T>(jsonData: Array<F>, mapper: (F) => T ): Immutable.List<T> {
    let list2 = [];
    for (var i = 0; i < jsonData.length; ++i) {
      list2.push(mapper(jsonData[i]));
    }
    return Immutable.List(list2);
  }

  parseProject(jsonData: any): Project {
    let that = this;
    let project = new Project({
      name: jsonData.name,
      incomes: that.parseList(jsonData.incomes, income => that.parseIncome(income)),
      expenses: that.parseList(jsonData.expenses, expense => that.parseExpense(expense)),
      categories: that.parseList(jsonData.categories, category => that.parseCategory(category)),
      foreignCurrency: jsonData.foreignCurrency,
    });
    return project
      .recomputeIncomes()
      .recomputeBudgetCategories();
  }

  parseExpense(jsonData: any): Expense {
    return new Expense({
      date: jsonData.date,
      localAmount: new BigNumber(jsonData.localAmount),
      accountNo: jsonData.accountNo,
      partner: jsonData.partner,
      category: jsonData.category
    });
  }

  parseIncome(jsonData: any): Income {
    return new Income({
      date: jsonData.date,
      foreignAmount: new BigNumber(jsonData.foreignAmount),
      exchangeRate: new BigNumber(jsonData.exchangeRate)
    });
  }

  parseCategory(jsonData: any): ProjectCategory {
    var that = this;
    return new ProjectCategory({
      tagName: jsonData.tagName,
      limitForeign: jsonData.limitForeign,
      limitPercentageForeign: jsonData.limitPercentageForeign
    });
  }

  parseTagNode(jsonData: any): TagNode {
    var that = this;
    return new TagNode({
      name: jsonData.name,
      subTags: that.parseList(jsonData.subTags, node => that.parseTagNode(node)),
    });
  }

  parseCurrency(jsonData: any): Currency {
    return new Currency({
      name: jsonData.name
    });
  }

}
