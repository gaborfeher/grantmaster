package com.github.gaborfeher.grantmaster.logic.entities;

import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.6.0.v20150309-rNA", date="2015-06-26T14:35:52")
@StaticMetamodel(ProjectBudgetLimit.class)
public class ProjectBudgetLimit_ { 

    public static volatile SingularAttribute<ProjectBudgetLimit, BigDecimal> budgetPercentage;
    public static volatile SingularAttribute<ProjectBudgetLimit, BudgetCategory> budgetCategory;
    public static volatile SingularAttribute<ProjectBudgetLimit, Project> project;
    public static volatile SingularAttribute<ProjectBudgetLimit, BigDecimal> budgetGrantCurrency;
    public static volatile SingularAttribute<ProjectBudgetLimit, Long> id;

}