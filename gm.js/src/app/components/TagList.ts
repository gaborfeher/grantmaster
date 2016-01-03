///<reference path='../data/TagNode.ts'/>

import {ChangeDetectionStrategy, Component, Input, View} from 'angular2/core';
import {NgFor} from 'angular2/common';
import {TagNode} from '../data/TagNode';
import {DataService} from './DataService';
import {TagName} from './TagName';
import {CellEntry} from './CellEntry';

@Component({
  selector: 'TagList',
  properties: [
    'path',
    'root',
    'years'
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
@View({
  templateUrl: './app/components/TagList.html',
  styleUrls: ['./app/components/TagList.css'],
  directives: [NgFor, CellEntry, TagName],
})
export class TagList {
  @Input() root: TagNode;
  @Input() path: Array<string>;
  dataService: DataService;

  columns: Array<string>;
  rows: Array<Object>;

  constructor(dataService: DataService) {
    this.dataService = dataService;
    var that = this;
    this.dataService.subscribe(function() {that.refreshCalculations(); } );
    this.refreshCalculations();
  }

  refreshCalculations() {
    this.columns = this.dataService.database.budgetCategories.summaries.keySeq().toArray();
    function getGlobalCategoryList(node: TagNode, path: Array<string | number>, list: Array<Object>) {
      list.push({node: node, path: path});
      node.subTags.forEach(
        (subTag, index) => {
          getGlobalCategoryList(subTag, path.concat(['subTags', index]), list);
          return true;
        });

      return list;
    }
    this.rows = getGlobalCategoryList(
      this.dataService.database.budgetCategories,
      ['budgetCategories'],
      []);
  }
}

