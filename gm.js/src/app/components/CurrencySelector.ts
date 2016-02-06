///<reference path='./StateService.ts'/>

import {Component, View, ChangeDetectionStrategy} from 'angular2/core';
import {NgFor, NgIf, NgModel} from 'angular2/common';
import {StateService} from './StateService';

@Component({
  selector: 'CurrencySelector',
  properties: [
    'value',
    'path',
    'currencies'
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
@View({
  templateUrl: 'app/components/CurrencySelector.html',
  styleUrls: ['app/components/CurrencySelector.css'],
  directives: [NgModel],
})
export class CurrencySelector {
  stateService: StateService;

  constructor(stateService: StateService) {
    this.stateService = stateService;
  }
}
