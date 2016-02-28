import {Component, View, ChangeDetectionStrategy} from 'angular2/core';
import {CurrencySelector} from './CurrencySelector';
import {Spreadsheet} from './Spreadsheet';

@Component({
  selector: 'ProjectViewer',
  properties: [
    'project',
    'path',
    'projectUIState',
    'currencyList',
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
@View({
  templateUrl: 'app/components/ProjectViewer.html',
  styleUrls: ['app/components/ProjectViewer.css'],
  directives: [Spreadsheet, CurrencySelector],
})
export class ProjectViewer {
}
