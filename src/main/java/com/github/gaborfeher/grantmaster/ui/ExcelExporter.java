package com.github.gaborfeher.grantmaster.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.DateFormatConverter;

class ExcelExporter {
  private final TableView table;
  
  ExcelExporter(TableView table) {
    this.table = table;
  }
  
  private void setExcelCell(HSSFWorkbook workbook, Object cellValue, Cell excelCell) {
    if (cellValue instanceof BigDecimal) {
      double doubleValue = ((BigDecimal)cellValue).doubleValue();
      excelCell.setCellValue(doubleValue);
      HSSFCellStyle cellStyle = workbook.createCellStyle();
      HSSFDataFormat hssfDataFormat = workbook.createDataFormat(); 
      cellStyle.setDataFormat(hssfDataFormat.getFormat("#,##0.00"));
      excelCell.setCellStyle(cellStyle);
      excelCell.setCellType(Cell.CELL_TYPE_NUMERIC);
    } else if (cellValue instanceof LocalDate) {
      LocalDate localDate = (LocalDate) cellValue;
      Calendar calendar = Calendar.getInstance();
      calendar.set(
          localDate.getYear(), 
          localDate.getMonthValue() - 1,
          localDate.getDayOfMonth());
      excelCell.setCellValue(calendar);
      
      String excelFormatPattern = DateFormatConverter.convert(Locale.US, "yyyy-MM-DD");
      CellStyle cellStyle = workbook.createCellStyle();
      DataFormat poiFormat = workbook.createDataFormat();
      cellStyle.setDataFormat(poiFormat.getFormat(excelFormatPattern));
      excelCell.setCellStyle(cellStyle);
    } else if (cellValue != null) {
      excelCell.setCellValue(cellValue.toString());
    }    
  }
  
  private int addExcelRow(HSSFWorkbook workbook, HSSFSheet sheet, int rowId, List<Object> data) {
    Row excelRow = sheet.createRow(rowId);
    int cellId = 0;
    for (Object cellValue : data) {
      Cell excelCell = excelRow.createCell(cellId);
      cellId += 1;
      setExcelCell(workbook, cellValue, excelCell);
    }
    return rowId + 1;
  }
  
  private Object getTableViewCellValue(TableColumn column, Object tableRowEntity) {
    TableColumn.CellDataFeatures cellDataFeatures =
        new TableColumn.CellDataFeatures(table, column, tableRowEntity);
    ObservableValue value = (ObservableValue) column.getCellValueFactory().call(cellDataFeatures);
    return value.getValue();
  }
  
  private List<Object> getTableViewColumns() {
    List visibleLeafColumns = table.getVisibleLeafColumns();
    // First column is the "delete" button, not interesting for exporting.
    return visibleLeafColumns.subList(1, visibleLeafColumns.size());
  }
  
  private List<Object> getTableViewRow(Object tableRowEntity) {
    List<Object> row = new ArrayList<>();
    for (Object columnObj : getTableViewColumns()) {
      TableColumn column = (TableColumn) columnObj;
      row.add(getTableViewCellValue(column, tableRowEntity));
    }
    return row;
  }
  
  private List<Object> getTableViewHeader() {
    List<Object> row = new ArrayList<>();
    for (Object columnObj : getTableViewColumns()) {
      TableColumn column = (TableColumn) columnObj;
      String title = column.getText();
      if (column.getParentColumn() != null) {
        title = column.getParentColumn().getText() + " " + title;
      }
      row.add(title);
    }
    return row;
  }
  
  public List<Object> getTableItems() {
    return table.getItems().subList(1, table.getItems().size());
  }
  
  private HSSFWorkbook createSpreadSheet() {
    HSSFWorkbook workbook = new HSSFWorkbook();
    HSSFSheet sheet = workbook.createSheet("GrantMaster exported data");
    int rowId = 0;
    rowId = addExcelRow(workbook, sheet, rowId, getTableViewHeader());
    for (Object tableRowEntity : getTableItems()) {
      rowId = addExcelRow(workbook, sheet, rowId, getTableViewRow(tableRowEntity));
    }
    return workbook;
  }
  
  public void saveSpreadSheet(HSSFWorkbook workbook, File file) {
    try (FileOutputStream out = new FileOutputStream(file)) {
      workbook.write(out);
    } catch (IOException ex) {
      Logger.getLogger(ExcelExporter.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public void export(File file) {
    HSSFWorkbook workbook = createSpreadSheet();
    saveSpreadSheet(workbook, file);
  }
  
}
