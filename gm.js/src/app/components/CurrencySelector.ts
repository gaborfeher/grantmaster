import {Component, ChangeDetectionStrategy, Input} from '@angular/core';
import {List} from 'immutable';

import {StateService} from 'app/components/StateService';
import {Currency} from 'app/state/database/Currency';

@Component({
  selector: 'CurrencySelector',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: 'app/components/CurrencySelector.html',
  styleUrls: ['app/components/CurrencySelector.css'],
})
export class CurrencySelector {
  @Input() value: string;
  @Input() path: any;  // TODO
  @Input() currencies: List<Currency>;

  stateService: StateService;

  constructor(stateService: StateService) {
    this.stateService = stateService;
  }
}
