package com.github.gaborfeher.grantmaster.logic.entities;

import com.github.gaborfeher.grantmaster.logic.entities.ProjectExpense;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectSource;
import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.6.0.v20150309-rNA", date="2015-06-26T14:35:52")
@StaticMetamodel(ExpenseSourceAllocation.class)
public class ExpenseSourceAllocation_ { 

    public static volatile SingularAttribute<ExpenseSourceAllocation, BigDecimal> accountingCurrencyAmount;
    public static volatile SingularAttribute<ExpenseSourceAllocation, Long> id;
    public static volatile SingularAttribute<ExpenseSourceAllocation, ProjectSource> source;
    public static volatile SingularAttribute<ExpenseSourceAllocation, ProjectExpense> expense;

}