package com.github.gaborfeher.grantmaster.logic.entities;

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import java.time.LocalDate;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.6.0.v20150309-rNA", date="2015-06-26T14:35:52")
@StaticMetamodel(ProjectReport.class)
public class ProjectReport_ { 

    public static volatile SingularAttribute<ProjectReport, String> note;
    public static volatile SingularAttribute<ProjectReport, LocalDate> reportDate;
    public static volatile SingularAttribute<ProjectReport, Project> project;
    public static volatile SingularAttribute<ProjectReport, Long> id;

}