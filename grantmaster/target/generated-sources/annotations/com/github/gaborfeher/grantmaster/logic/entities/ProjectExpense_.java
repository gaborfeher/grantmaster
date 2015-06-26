package com.github.gaborfeher.grantmaster.logic.entities;

import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.entities.ExpenseSourceAllocation;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectReport;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.6.0.v20150309-rNA", date="2015-06-26T14:35:52")
@StaticMetamodel(ProjectExpense.class)
public class ProjectExpense_ { 

    public static volatile SingularAttribute<ProjectExpense, BigDecimal> originalAmount;
    public static volatile ListAttribute<ProjectExpense, ExpenseSourceAllocation> sourceAllocations;
    public static volatile SingularAttribute<ProjectExpense, String> comment2;
    public static volatile SingularAttribute<ProjectExpense, String> partnerName;
    public static volatile SingularAttribute<ProjectExpense, String> comment1;
    public static volatile SingularAttribute<ProjectExpense, String> accountNo;
    public static volatile SingularAttribute<ProjectExpense, ProjectReport> report;
    public static volatile SingularAttribute<ProjectExpense, Project> project;
    public static volatile SingularAttribute<ProjectExpense, BudgetCategory> budgetCategory;
    public static volatile SingularAttribute<ProjectExpense, Long> id;
    public static volatile SingularAttribute<ProjectExpense, LocalDate> paymentDate;
    public static volatile SingularAttribute<ProjectExpense, Currency> originalCurrency;

}