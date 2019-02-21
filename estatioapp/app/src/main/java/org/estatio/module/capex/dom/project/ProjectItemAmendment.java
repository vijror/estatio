package org.estatio.module.capex.dom.project;

import java.math.BigDecimal;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Programmatic;

import org.isisaddons.module.security.dom.tenancy.ApplicationTenancy;

import org.incode.module.base.dom.types.MoneyType;
import org.incode.module.base.dom.utils.TitleBuilder;

import org.estatio.module.base.dom.UdoDomainObject;

import lombok.Getter;
import lombok.Setter;

@PersistenceCapable(
        identityType = IdentityType.DATASTORE
        ,schema = "dbo"
)
@DatastoreIdentity(strategy = IdGeneratorStrategy.NATIVE, column = "id")
@Version(strategy = VersionStrategy.VERSION_NUMBER, column = "version")
@Queries({
        @Query(name = "findByProjectItem", language = "JDOQL", value = "SELECT "
                + "FROM org.estatio.module.capex.dom.project.ProjectItemAmendment "
                + "WHERE projectItem == :projectItem "),
        @Query(name = "findUnique", language = "JDOQL", value = "SELECT "
                + "FROM org.estatio.module.capex.dom.project.ProjectItemAmendment "
                + "WHERE projectItem == :projectItem "
                + " && date == :date")
})
@Unique(name = "ProjectItemAmendment_projectItem_date_UNQ", members = { "projectItem", "date" })
@DomainObject(
        editing = Editing.DISABLED,
        objectType = "org.estatio.capex.dom.project.ProjectItemAmendment"
)
public class ProjectItemAmendment extends UdoDomainObject<ProjectItemAmendment> {

    public String title(){
        return TitleBuilder.start().withParent(getProjectItem()).withName(getDate()).toString();
    }

    public ProjectItemAmendment() {
        super("projectItem, date");
    }

    @Getter @Setter
    @Column(allowsNull = "false", name = "projectItemId")
    private ProjectItem projectItem;

    @Getter @Setter
    @Column(allowsNull = "false")
    private LocalDate date;

    @Getter @Setter
    @Column(allowsNull = "true", scale = MoneyType.Meta.SCALE)
    private BigDecimal newBudgetedAmount;

    @Getter @Setter
    @Column(allowsNull = "true", scale = MoneyType.Meta.SCALE)
    private BigDecimal previousBudgetedAmount;

    @Override
    @Programmatic
    public ApplicationTenancy getApplicationTenancy() {
        return getProjectItem().getApplicationTenancy();
    }

}
