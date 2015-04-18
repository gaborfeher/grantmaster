package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.ExpenseType;
import com.github.gaborfeher.grantmaster.core.RefreshControlSingleton;
import com.github.gaborfeher.grantmaster.core.RefreshMessage;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.ExpenseTypeSummary;
import com.github.gaborfeher.grantmaster.logic.wrappers.ExpenseTypeWrapper;
import com.github.gaborfeher.grantmaster.ui.cells.DoubleTableCellFactory;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import javax.persistence.EntityManager;

public class ExpenseTypesTabController extends RefreshControlSingleton.MessageObserver implements Initializable {
  @FXML TableView<ExpenseTypeWrapper> table;

  public ExpenseTypesTabController() {
  }
  
  @Override
  public void refresh(RefreshMessage message) {
    List<ExpenseTypeWrapper> expenseTypes = ExpenseTypeWrapper.getExpenseTypes();
    Map<Integer, ExpenseTypeWrapper> expenseTypeMap = new HashMap<>();
    for (ExpenseTypeWrapper expenseTypeWrapper : expenseTypes) {
      expenseTypeMap.put(expenseTypeWrapper.getId(), expenseTypeWrapper);
    }
    
    // Collect expense summaries.
    EntityManager em = DatabaseConnectionSingleton.getInstance().em();
    List<Object[]> expenseSummaryList = em.createQuery(
        "SELECT e.expenseType, e.project.accountCurrency.code AS currency, FUNCTION('YEAR', e.paymentDate) AS year, SUM(a.accountingCurrencyAmount) " +
        "FROM ProjectExpense e, ExpenseSourceAllocation a " +
        "WHERE a.expense = e " +
        "GROUP BY e.expenseType, year, currency").
        getResultList();
    Set<String> columnNames = new TreeSet<>();
    for (Object[] line : expenseSummaryList) {
      ExpenseTypeWrapper expenseTypeWrapper = expenseTypeMap.get(((ExpenseType)line[0]).getId());
      int year = (Integer)line[2];
      String header = String.format("%d (%s)", year, (String)line[1]);
      columnNames.add(header);
      expenseTypeWrapper.addSummaryValue(header, (Double)line[3]);
    }

    // Collect income summaries.
    List<Object[]> incomeSummaryList = em.createQuery(
        "SELECT s.project.incomeType AS incomeType, s.project.accountCurrency.code AS currency, FUNCTION('YEAR', s.availabilityDate) AS year, SUM(s.amount * s.exchangeRate) " +
        "FROM ProjectSource s " +
        "GROUP BY incomeType, year, currency").
        getResultList();
    for (Object[] line : incomeSummaryList) {
      ExpenseTypeWrapper expenseTypeWrapper = expenseTypeMap.get(((ExpenseType)line[0]).getId());
      int year = (Integer)line[2];
      String header = String.format("%d (%s)", year, (String)line[1]);
      columnNames.add(header);
      expenseTypeWrapper.addSummaryValue(header, (Double)line[3]);
    }
    
    // Initialize columns.
    table.getColumns().remove(4, table.getColumns().size());
    for (final String columnName : columnNames) {
      TableColumn column = new TableColumn<ExpenseTypeWrapper, Double>(columnName);
      column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ExpenseTypeWrapper, Double>,ObservableValue<Double>>() {
        @Override
        public ObservableValue<Double> call(TableColumn.CellDataFeatures<ExpenseTypeWrapper, Double> p) {
          Double value = p.getValue().getSummaryValue(columnName);
          return new ReadOnlyObjectWrapper<Double>(value);
        }
      });
      column.setCellFactory(new DoubleTableCellFactory());
      column.getStyleClass().add("numColumn");
      column.setSortable(false);
      column.setEditable(false);
      table.getColumns().add(column);
    }
    
    // Insert rows summary rows.
    table.getItems().clear();
    ExpenseTypeSummary totalSum = new ExpenseTypeSummary("Kiadások összesen:");
    ExpenseTypeSummary groupSum = null;
    String currentGroupName = null;
    ExpenseTypeWrapper previous = null;
    for (ExpenseTypeWrapper current : expenseTypes) {
      if (previous != null) {
        if (currentGroupName != null && !currentGroupName.equals(current.getGroupName())) {
          table.getItems().add(groupSum);
          currentGroupName = null;
          groupSum = null;
        }
        
        if (previous.getDirection() != current.getDirection()) {
          table.getItems().add(totalSum);
          totalSum = new ExpenseTypeSummary("Bevételek összesen");
        }
      }
      
      if (current.getGroupName() != null) {
        if (currentGroupName == null || groupSum == null) {
          currentGroupName = current.getGroupName();
          groupSum = new ExpenseTypeSummary(current.getGroupName() + " összesen");
        }
        groupSum.addSummaryValues(current);
      }
      
      table.getItems().add(current);
      totalSum.addSummaryValues(current);
      
      previous = current;
    }
    if (groupSum != null) {
      table.getItems().add(groupSum);
    }
    table.getItems().add(totalSum);
    
  }

  /**
   * Initializes the controller class.
   */
  @Override
  public void initialize(URL url, ResourceBundle rb) {
    RefreshControlSingleton.getInstance().subscribe(this);
  }  

  @FXML
  public void handleExpenseTypeAddButtonAction(ActionEvent event) {
    ExpenseType expenseType = new ExpenseType();
    ExpenseTypeWrapper wrapper = new ExpenseTypeWrapper(expenseType);
    wrapper.setState(EntityWrapper.State.EDITING_NEW);
    table.getItems().add(wrapper);
  }

}
