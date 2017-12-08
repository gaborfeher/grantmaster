import {List} from 'immutable';
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
  @Input() value: string;
  @Input() path: Array<any>;  // TODO
  list: List<{key: string, value: string}>;

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
