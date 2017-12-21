import {Input, Component, ChangeDetectionStrategy, SimpleChange} from '@angular/core';

import {StateService} from 'app/components/StateService';
import {BigNumber} from 'bignumber.js';
import {TableColumn} from 'app/state/ui/TableColumn';
import {Utils} from 'app/utils/Utils';

import {System} from 'systemjs';
let dialog = System._nodeRequire('electron').remote.dialog;

@Component({
  selector: 'CellEntry',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: 'app/components/CellEntry.html',
  styleUrls: ['app/components/CellEntry.css'],
})
export class CellEntry {
  @Input() item: any;
  @Input() column: TableColumn;
  @Input() path: Array<string>;
  @Input() creationMode: boolean;

  editMode: boolean;
  masterValue: string;
  value: string;
  flatPath: any;
  stateService: StateService;

  constructor(stateService: StateService) {
    this.editMode = false;
    this.stateService = stateService;
  }

  ngOnChanges(chg: SimpleChange) {
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

  validate(): Array<string> {
    let value: string = this.value;
    let errors: Array<string> = [];

    if (!this.column.constraints) {
      return [];
    }

    if (this.column.constraints.indexOf('not_null') >= 0) {
      if (value === '' || value === undefined) {
        errors.push('must not be empty');
        return errors;
      }
    }

    if (this.column.kind === 'number') {
      if (value === '' || value === undefined) {
        return [];  // null values are accepted (see 'not_null' constraint above)
      }

      if (value.indexOf('.') !== value.lastIndexOf('.')) {
        errors.push('Invalid number');
        return errors;
      }
      var i = 0;
      if (value.length > 0 && value[i] === '-') {
        i += 1;
      }
      while (i < value.length) {
        if (value[i] != '.' && (value[i] < '0' || value[i] > '9')) {
          errors.push('Invalid number');
          return errors;
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
    return errors;
  }

  valueAtLastStart: string = undefined;

  validateAndApplyEdit(): Array<string> {
    let that: CellEntry = this;
    function parseValue(value: string): BigNumber | string {
      if (that.column.kind === 'number') {
        if (value !== '' && value !== undefined) {
          return new BigNumber(value);
        } else {
          return undefined;
        }
      } else {
        return value;
      }
    }
    let errors = this.validate();
    if (errors.length === 0) {
      let val: BigNumber | string = parseValue(this.value);
      errors = errors.concat(
          this.stateService.setByPath(this.path, val));
    }
    return errors;
  }

  commitEdit(ref: HTMLElement, focused: boolean) {
    if (!this.editMode) {
      return;
    }

    if (this.value !== this.masterValue) {
      let val = this.value;
      let errors = this.validateAndApplyEdit();
      if (errors.length === 0) {  // Edit applied.
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
      } else {  // Edit apply failed.
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

  onBlur(ref: HTMLElement) {
    this.commitEdit(ref, false);
  }

  onFocus() {
    this.startEdit();
  }

  onEnter(ref: HTMLElement) {
    this.commitEdit(ref, true);
  }

}
