///<reference path='./StateService.ts'/>

import {Input, Component, ChangeDetectionStrategy} from '@angular/core';
import {StateService} from 'app/components/StateService';
import {TagNode} from 'app/state/database/TagNode';

@Component({
  selector: 'BudgetCategorySelector',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: 'app/components/BudgetCategorySelector.html',
  styleUrls: ['app/components/BudgetCategorySelector.css'],
})
export class BudgetCategorySelector {
  @Input() root: TagNode;
  @Input() value: any;  // TODO
  @Input() path: any;  // TODO
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
