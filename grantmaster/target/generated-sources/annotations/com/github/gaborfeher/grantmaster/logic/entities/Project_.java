package com.github.gaborfeher.grantmaster.logic.entities;

import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory;
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectReport;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.6.0.v20150309-rNA", date="2015-06-26T14:35:52")
@StaticMetamodel(Project.class)
public class Project_ { 

    public static volatile ListAttribute<Project, ProjectReport> reports;
    public static volatile SingularAttribute<Project, BudgetCategory> incomeType;
    public static volatile SingularAttribute<Project, String> name;
    public static volatile SingularAttribute<Project, Currency> accountCurrency;
    public static volatile SingularAttribute<Project, Long> id;
    public static volatile SingularAttribute<Project, Currency> grantCurrency;

}