package com.github.gaborfeher.grantmaster.logic.entities;

import com.github.gaborfeher.grantmaster.logic.entities.BudgetCategory.Direction;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.6.0.v20150309-rNA", date="2015-06-26T14:35:52")
@StaticMetamodel(BudgetCategory.class)
public class BudgetCategory_ { 

    public static volatile SingularAttribute<BudgetCategory, String> groupName;
    public static volatile SingularAttribute<BudgetCategory, String> name;
    public static volatile SingularAttribute<BudgetCategory, Long> id;
    public static volatile SingularAttribute<BudgetCategory, Direction> direction;

}