
import {Input, Component, ChangeDetectionStrategy} from '@angular/core';

import {StateService} from 'app/components/StateService';
import {BigNumber} from 'bignumber.js';
import {TableColumn} from 'app/state/ui/TableColumn';
import {Utils} from 'app/utils/Utils';

declare var System;
let fs = System._nodeRequire('fs');
let dialog = System._nodeRequire('electron').remote.dialog;

@Component({
  selector: 'CellEntry',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: 'app/components/CellEntry.html',
  styleUrls: ['app/components/CellEntry.css'],
})
export class CellEntry {
  @Input() item;
  @Input() column: TableColumn;
  @Input() path: Array<string>;
  @Input() creationMode: any;  // TODO

  editMode: boolean;
  masterValue: string;
  value: string;
  flatPath: any;
  stateService: StateService;

  constructor(stateService: StateService) {
    this.editMode = false;
    this.stateService = stateService;
  }

  ngOnChanges(chg) {
    this.masterValue = this.getFormattedValue();
    this.value = this.masterValue;
  }

  getRawValue(): any {
    return this.item.get(this.column.key);
  }

  getFormattedValue() {
    let val = this.getRawValue();
    if (val === undefined) {
      return '';
    } else {
      if (this.column.kind === 'number') {
        return Utils.bigFormat(val);
      } else {
        return val;
      }
    }
  }

  startEdit() {
    this.editMode = true;
  }

  cancelEdit() {
    this.editMode = false;
    this.value = this.masterValue;
  }

  resetEdit() {
    this.value = this.masterValue;
  }

  validate(value: string, errors: Array<string>): boolean {
    if (!this.column.constraints) {
      return true;
    }

    if (this.column.constraints.indexOf('not_null') >= 0) {
      if (value === '' || value === undefined) {
        errors.push('must not be empty');
        return false;
      }
    }

    if (this.column.kind === 'number') {
      if (value === '' || value === undefined) {
        return true;  // null values are accepted (see 'not_null' constraint above)
      }

      if (value.indexOf('.') !== value.lastIndexOf('.')) {
        errors.push('Invalid number');
        return false;
      }
      var i = 0;
      if (value.length > 0 && value[i] === '-') {
        i += 1;
      }
      while (i < value.length) {
        if (value[i] != '.' && (value[i] < '0' || value[i] > '9')) {
          errors.push('Invalid number');
          return false;
        }
        i += 1;
      }
      var numberValue = new BigNumber(value);
      for (var j = 0; j < this.column.constraints.size; ++j) {
        var constraint = this.column.constraints.get(j);
        if (constraint === 'positive') {
          if (numberValue !== undefined) {
            if (numberValue.isNegative() || numberValue.isZero()) {
              errors.push('number should be positive');
            }
          }
          continue;
        }
        if (constraint.indexOf(':') >= 0) {
          let cs = constraint.split(':');
          let c = cs[0];
          let limit = new BigNumber(cs[1]);
          if (c === 'min') {
            if (numberValue.lessThan(limit)) {
              errors.push('number should be at least ' + cs[1]);
            }
            continue;
          } else if (c === 'max') {
            if (numberValue.greaterThan(limit)) {
              errors.push('number should be no greater than ' + cs[1]);
            }
            continue;
          }
        }
      }
    } else if (this.column.kind === 'date') {
      if (!Utils.validateDate(value)) {
        errors.push('Invalid date format, use ISO format: YYYY-MM-DD');
      }
    }
    return errors.length === 0;
  }

  valueAtLastStart: string = undefined;

  commitEdit(ref, focused: boolean) {
    if (!this.editMode) {
      return;
    }

    if (this.value !== this.masterValue) {
      let val = this.value;
      let errors = [];
      if (this.validate(val, errors)) {
        let val2: BigNumber | string = val;
        if (this.column.kind === 'number') {
          if (val !== '' && val !== undefined) {
            val2 = new BigNumber(val);
          } else {
            val2 = undefined;
          }
        }
        this.stateService.setByPath(this.path, val2);

        // Leave edit mode. (If this edit change triggers a reordering of spreadsheet rows
        // then Angular sometimes sends an onBlur event. If editMode == true then it would
        // cause a mess.)
        this.editMode = false;
        if (focused) {
          // Return to edit mode if this commit was triggered by an enter key.
          setTimeout(() => {
            ref.focus();  // in case focus was lost
            this.editMode = true;  // in case focus was not lost
          }, 0);
        }
      } else {
        if (focused) {
          ref.blur();  // this will trigger commitEdit again
          return;
        }

        let userChoice: number = dialog.showMessageBox({
          type: 'warning',
          buttons: ['OK', 'Cancel'],
          title: 'Validation error',
          cancelId: 1,
          message: errors.join('\n') + '\n\nContinue editing?'
        });
        if (userChoice == 0) {
          setTimeout(() => ref.focus(), 0);
        } else {
          this.cancelEdit();
          setTimeout(() => ref.focus(), 0);  // this is only to trigger refresh
        }
      }
    }
  }

  onBlur(ref) {
    this.commitEdit(ref, false);
  }

  onFocus() {
    this.startEdit();
  }

  onEnter(ref) {
    this.commitEdit(ref, true);
  }

}
