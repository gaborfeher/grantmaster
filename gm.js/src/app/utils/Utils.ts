

export namespace Utils {
  export function validateDate(date: string): boolean {
    for (var i = 0; i < date.length; ++i) {
      if (date[i] === '-' || date[i] >= '0' && date[i] <= '9') {
        // ok
      } else {
        return false;
      }
    }

    var dateParts = date.split('-');
    if (dateParts.length != 3) {
      return false;
    }
    var y = parseInt(dateParts[0]);
    var m = parseInt(dateParts[1]);
    var d = parseInt(dateParts[2]);
    if (y < 0 || y > 3000) {
      return false;
    }
    if (m < 1 || m > 12) {
      return false;
    }
    if (d < 1 || d > 31) {
      return false;
    }
    return true;
  }
}
