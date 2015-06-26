package com.github.gaborfeher.grantmaster.logic.entities;

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectReport;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.6.0.v20150309-rNA", date="2015-06-26T14:35:52")
@StaticMetamodel(ProjectSource.class)
public class ProjectSource_ { 

    public static volatile SingularAttribute<ProjectSource, BigDecimal> exchangeRate;
    public static volatile SingularAttribute<ProjectSource, BigDecimal> grantCurrencyAmount;
    public static volatile SingularAttribute<ProjectSource, ProjectReport> report;
    public static volatile SingularAttribute<ProjectSource, Project> project;
    public static volatile SingularAttribute<ProjectSource, LocalDate> availabilityDate;
    public static volatile SingularAttribute<ProjectSource, Long> id;

}