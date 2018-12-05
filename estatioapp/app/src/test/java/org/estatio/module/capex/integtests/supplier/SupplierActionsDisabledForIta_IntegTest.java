package org.estatio.module.capex.integtests.supplier;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.togglz.junit.TogglzRule;

import org.apache.isis.applib.fixtures.FixtureClock;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.sudo.SudoService;

import org.estatio.module.asset.fixtures.person.enums.Person_enum;
import org.estatio.module.base.spiimpl.togglz.EstatioTogglzFeature;
import org.estatio.module.capex.dom.invoice.IncomingInvoiceRoleTypeEnum;
import org.estatio.module.capex.integtests.CapexModuleIntegTestAbstract;
import org.estatio.module.party.dom.Organisation;
import org.estatio.module.party.dom.Person;
import org.estatio.module.party.fixtures.organisation.enums.Organisation_enum;

public class SupplierActionsDisabledForIta_IntegTest extends CapexModuleIntegTestAbstract {

    Organisation supplier;

    Person italianUser;

    Person frenchUser;


    @Before
    public void setupData() {

        runFixtureScript(new FixtureScript() {
            @Override
            protected void execute(final ExecutionContext ec) {
                ec.executeChildren(this,
                        Organisation_enum.TopModelIt,
                        Person_enum.CarmenIncomingInvoiceManagerIt,
                        Person_enum.BertrandIncomingInvoiceManagerFr);
            }
        });
    }

    @Before
    public void setUp() {
        supplier = Organisation_enum.TopModelIt.findUsing(serviceRegistry);
        supplier.addRole(IncomingInvoiceRoleTypeEnum.SUPPLIER);

        italianUser = Person_enum.CarmenIncomingInvoiceManagerIt.findUsing(serviceRegistry);
        frenchUser = Person_enum.BertrandIncomingInvoiceManagerFr.findUsing(serviceRegistry);

    }

    @Rule
    public TogglzRule togglzRule = TogglzRule.allDisabled(EstatioTogglzFeature.class);

    @Test
    public void  french_user_can_modify_supplier() throws Exception {

        // when
        queryResultsCache.resetForNextTransaction(); // workaround: clear MeService#me cache
        sudoService.sudo(frenchUser.getUsername(), (Runnable) () ->
                wrap(supplier).changeName("new name", FixtureClock.getTimeAsLocalDate()));

        // then
        Assertions.assertThat(supplier.getName()).isEqualTo("new name");

    }

    @Inject
    QueryResultsCache queryResultsCache;

    @Inject
    SudoService sudoService;

}

