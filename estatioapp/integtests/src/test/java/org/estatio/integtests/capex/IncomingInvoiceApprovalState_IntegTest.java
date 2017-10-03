package org.estatio.integtests.capex;

import java.util.SortedSet;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.togglz.junit.TogglzRule;

import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.services.sudo.SudoService;
import org.apache.isis.applib.services.wrapper.DisabledException;

import org.incode.module.country.dom.impl.Country;
import org.incode.module.country.dom.impl.CountryRepository;
import org.incode.module.country.fixture.CountriesRefData;

import org.estatio.capex.dom.bankaccount.verification.BankAccountVerificationState;
import org.estatio.capex.dom.bankaccount.verification.BankAccount_verificationState;
import org.estatio.capex.dom.invoice.IncomingInvoice;
import org.estatio.capex.dom.invoice.IncomingInvoiceRepository;
import org.estatio.capex.dom.invoice.approval.IncomingInvoiceApprovalState;
import org.estatio.capex.dom.invoice.approval.IncomingInvoiceApprovalStateTransition;
import org.estatio.capex.dom.invoice.approval.triggers.IncomingInvoice_complete;
import org.estatio.capex.dom.project.Project;
import org.estatio.capex.dom.project.ProjectRepository;
import org.estatio.dom.asset.Property;
import org.estatio.dom.asset.PropertyRepository;
import org.estatio.dom.charge.Charge;
import org.estatio.dom.charge.ChargeRepository;
import org.estatio.dom.financial.bankaccount.BankAccount;
import org.estatio.dom.financial.bankaccount.BankAccountRepository;
import org.estatio.dom.party.Party;
import org.estatio.dom.party.PartyRepository;
import org.estatio.dom.party.PartyRoleTypeEnum;
import org.estatio.dom.party.Person;
import org.estatio.dom.party.role.PartyRole;
import org.estatio.dom.togglz.EstatioTogglzFeature;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.asset.PropertyForOxfGb;
import org.estatio.fixture.financial.BankAccountForTopModelGb;
import org.estatio.fixture.invoice.IncomingInvoiceFixture;
import org.estatio.fixture.party.OrganisationForHelloWorldGb;
import org.estatio.fixture.party.OrganisationForTopModelGb;
import org.estatio.fixture.party.PersonForEmmaFarmerGb;
import org.estatio.integtests.EstatioIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.estatio.capex.dom.bankaccount.verification.BankAccountVerificationState.NOT_VERIFIED;

public class IncomingInvoiceApprovalState_IntegTest extends EstatioIntegrationTest {

    Property propertyForOxf;
    Party buyer;
    Party seller;

    Country greatBritain;
    Charge charge_for_works;

    IncomingInvoice incomingInvoice;

    BankAccount bankAccount;
    Project project;

    @Before
    public void setupData() {

        runFixtureScript(new FixtureScript() {
            @Override
            protected void execute(final FixtureScript.ExecutionContext executionContext) {
                executionContext.executeChild(this, new EstatioBaseLineFixture());
                executionContext.executeChild(this, new IncomingInvoiceFixture());
                executionContext.executeChild(this, new BankAccountForTopModelGb());
                executionContext.executeChild(this, new PersonForEmmaFarmerGb());
            }
        });

        TickingFixtureClock.replaceExisting();
    }

    @Before
    public void setUp() {
        propertyForOxf = propertyRepository.findPropertyByReference(PropertyForOxfGb.REF);

        buyer = partyRepository.findPartyByReference(OrganisationForHelloWorldGb.REF);
        seller = partyRepository.findPartyByReference(OrganisationForTopModelGb.REF);

        greatBritain = countryRepository.findCountry(CountriesRefData.GBR);
        charge_for_works = chargeRepository.findByReference("WORKS");

        project = projectRepository.findByReference("OXF-02");

        bankAccount = bankAccountRepository.findBankAccountByReference(seller, BankAccountForTopModelGb.REF);

        incomingInvoice = incomingInvoiceRepository.findByInvoiceNumberAndSellerAndInvoiceDate("65432", seller, new LocalDate(2014,5,13));
        incomingInvoice.setBankAccount(bankAccount);

        assertThat(incomingInvoice).isNotNull();
        assertThat(incomingInvoice.getApprovalState()).isEqualTo(IncomingInvoiceApprovalState.NEW);
        assertState(bankAccount, NOT_VERIFIED);

    }

    @After
    public void tearDown() {
        TickingFixtureClock.reinstateExisting();
    }

    @Rule
    public TogglzRule togglzRule = TogglzRule.allDisabled(EstatioTogglzFeature.class);

    @Test
    public void complete_should_fail_when_not_having_appropriate_role_test() {

        Exception error = new Exception();

        // given
        Person personEmmaWithNoRoleAsPropertyManager = (Person) partyRepository.findPartyByReference(PersonForEmmaFarmerGb.REF);
        SortedSet<PartyRole> rolesforEmma = personEmmaWithNoRoleAsPropertyManager.getRoles();
        assertThat(rolesforEmma.size()).isEqualTo(1);
        assertThat(rolesforEmma.first().getRoleType().getKey()).isEqualTo(PartyRoleTypeEnum.TREASURER.getKey());

        // when
        try {
            sudoService.sudo(PersonForEmmaFarmerGb.REF.toLowerCase(), (Runnable) () ->
                    wrap(mixin(IncomingInvoice_complete.class, incomingInvoice)).act("PROPERTY_MANAGER", null, null));
        } catch (DisabledException e){
            error = e;
        }

        assertThat(error.getMessage()).isNotNull();

    }

    void assertState(final BankAccount bankAccount, final BankAccountVerificationState expected) {
        assertThat(wrap(mixin(BankAccount_verificationState.class, bankAccount)).prop()).isEqualTo(
                expected);
    }

    @Inject
    SudoService sudoService;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    IncomingInvoiceApprovalStateTransition.Repository incomingInvoiceStateTransitionRepository;

    @Inject
    IncomingInvoiceRepository incomingInvoiceRepository;

    @Inject
    PropertyRepository propertyRepository;

    @Inject
    PartyRepository partyRepository;

    @Inject
    CountryRepository countryRepository;

    @Inject
    ChargeRepository chargeRepository;

    @Inject
    BankAccountRepository bankAccountRepository;

}

