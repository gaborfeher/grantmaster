///<reference path='./StateService.ts'/>

import {Component, ChangeDetectionStrategy, Input} from '@angular/core';
import {StateService} from './StateService';

@Component({
  selector: 'CurrencySelector',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: 'app/components/CurrencySelector.html',
  styleUrls: ['app/components/CurrencySelector.css'],
})
export class CurrencySelector {
  @Input() value: any;  // TODO
  @Input() path: any;  // TODO
  @Input() currencies: any;  // TODO

  stateService: StateService;

  constructor(stateService: StateService) {
    this.stateService = stateService;
  }
}
