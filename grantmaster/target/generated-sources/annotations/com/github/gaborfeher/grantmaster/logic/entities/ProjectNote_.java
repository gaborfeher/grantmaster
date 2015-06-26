package com.github.gaborfeher.grantmaster.logic.entities;

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import java.sql.Timestamp;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.6.0.v20150309-rNA", date="2015-06-26T14:35:52")
@StaticMetamodel(ProjectNote.class)
public class ProjectNote_ { 

    public static volatile SingularAttribute<ProjectNote, String> note;
    public static volatile SingularAttribute<ProjectNote, Timestamp> entryTime;
    public static volatile SingularAttribute<ProjectNote, Project> project;
    public static volatile SingularAttribute<ProjectNote, Long> id;

}