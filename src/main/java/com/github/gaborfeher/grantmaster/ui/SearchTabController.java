package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.ui.framework.TablePageControllerBase;
import com.github.gaborfeher.grantmaster.core.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectExpense;
import com.github.gaborfeher.grantmaster.logic.wrappers.GlobalBudgetCategoryWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectExpenseWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectWrapper;
import java.util.List;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import javax.persistence.EntityManager;

public class SearchTabController
    extends TablePageControllerBase<ProjectExpenseWrapper> {
  @FXML private ExpenseTableController tableController;
  
  @FXML private ChoiceBox<Project> project;
  @FXML private DatePicker startDate;
  @FXML private DatePicker endDate;
  @FXML private ChoiceBox<BudgetCategory> budgetCategory;
  @FXML private TextField budgetCategoryGroup;
  @FXML private TextField accountNo;
  @FXML private TextField partnerName;
  @FXML private TextField comment1;
  @FXML private TextField comment2;
  
  private List<ProjectExpenseWrapper> searchResults;
  
  @FXML
  private void search() {
    DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      searchResults = ProjectExpenseWrapper.getExpenseList(
          em,
          project.getValue(),
          startDate.getValue(),
          endDate.getValue(),
          budgetCategory.getValue(),
          budgetCategoryGroup.getText(),
          accountNo.getText(),
          partnerName.getText(),
          comment1.getText(),
          comment2.getText());
      return true;
    });
    onRefresh();
  }

  @Override
  public void getItemListForRefresh(EntityManager em, List<ProjectExpenseWrapper> items) {
    items.clear();
    if (searchResults != null) {
      items.addAll(searchResults);
    }
  }

  @Override
  protected void refreshOtherContent() {
    DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      budgetCategory.getItems().clear();
      budgetCategory.getItems().add(null);
      budgetCategory.getItems().addAll(GlobalBudgetCategoryWrapper.getBudgetCategories(em, BudgetCategory.Direction.PAYMENT));
      project.getItems().clear();
      project.getItems().add(null);
      project.getItems().addAll(ProjectWrapper.getProjectsWithoutWrapping(em));

      tableController.accountingCurrencyAmountColumn.setCellValueFactory(
          new Callback<TableColumn.CellDataFeatures<ProjectExpenseWrapper, Object>, ObservableValue<Object>>() {
        @Override
        public ObservableValue<Object> call(TableColumn.CellDataFeatures<ProjectExpenseWrapper, Object> p) {
          ProjectExpenseWrapper expenseWrapper = p.getValue();
          ProjectExpense expense = p.getValue().getEntity();
          String result =
              String.format(
                  "%2.2f %s",
                  expenseWrapper.getAccountingCurrencyAmount(),
                  expense.getProject().getAccountCurrency().getCode());
          return new ReadOnlyObjectWrapper<Object>(result);
        }
      });
      tableController.accountingCurrencyAmountColumn.setCellFactory(
          new Callback<TableColumn<ProjectExpenseWrapper, Object>, TableCell<ProjectExpenseWrapper, Object>>() {
        @Override
        public TableCell<ProjectExpenseWrapper, Object> call(TableColumn<ProjectExpenseWrapper, Object> p) {
          return (TableCell<ProjectExpenseWrapper, Object>) TableColumn.DEFAULT_CELL_FACTORY.call(p);
        }
      });

      tableController.grantCurrencyAmountColumn.setCellValueFactory(
          new Callback<TableColumn.CellDataFeatures<ProjectExpenseWrapper, Object>, ObservableValue<Object>>() {
        @Override
        public ObservableValue<Object> call(TableColumn.CellDataFeatures<ProjectExpenseWrapper, Object> p) {
          ProjectExpenseWrapper expenseWrapper = p.getValue();
          ProjectExpense expense = p.getValue().getEntity();
          String result =
              String.format(
                  "%2.2f %s",
                  expenseWrapper.getGrantCurrencyAmount(),
                  expense.getProject().getGrantCurrency());
          return new ReadOnlyObjectWrapper<Object>(result);
        }
      });
      tableController.grantCurrencyAmountColumn.setCellFactory(
          new Callback<TableColumn<ProjectExpenseWrapper, Object>, TableCell<ProjectExpenseWrapper, Object>>() {
        @Override
        public TableCell<ProjectExpenseWrapper, Object> call(TableColumn<ProjectExpenseWrapper, Object> p) {
          return (TableCell<ProjectExpenseWrapper, Object>) TableColumn.DEFAULT_CELL_FACTORY.call(p);
        }
      });
      return true;
    });
  }

  @Override
  protected ProjectExpenseWrapper createNewEntity(EntityManager em) {
    return null;
  }
  
}
