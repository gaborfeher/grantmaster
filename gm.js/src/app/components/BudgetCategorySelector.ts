///<reference path='./StateService.ts'/>

import {Input, Component, View, ChangeDetectionStrategy} from 'angular2/core';
import {NgFor, NgIf, NgModel} from 'angular2/common';
import {StateService} from './StateService';
import {TagNode} from '../state/database/TagNode';

@Component({
  selector: 'BudgetCategorySelector',
  properties: [
    'value',
    'path',
    'root'
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
@View({
  templateUrl: 'app/components/BudgetCategorySelector.html',
  styleUrls: ['app/components/BudgetCategorySelector.css'],
  directives: [NgModel],
})
export class BudgetCategorySelector {
  @Input() root: TagNode;
  list: any;

  stateService: StateService;

  constructor(stateService: StateService) {
    this.stateService = stateService;
  }

  ngOnChanges(chg) {
    if (chg['root']) {
      this.list = this.root.getSubTreeAsUIList();
    }
  }
}
