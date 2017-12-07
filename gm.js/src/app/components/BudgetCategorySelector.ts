///<reference path='./StateService.ts'/>

import {Input, Component, ChangeDetectionStrategy} from '@angular/core';
import {NgForOf, NgIf} from '@angular/common';
import {NgModel} from '@angular/forms';
import {StateService} from './StateService';
import {TagNode} from '../state/database/TagNode';

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
