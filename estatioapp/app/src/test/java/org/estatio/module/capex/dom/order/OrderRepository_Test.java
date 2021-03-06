package org.estatio.module.capex.dom.order;

import org.joda.time.LocalDate;
import org.junit.Test;

import org.estatio.module.asset.dom.Property;
import org.estatio.module.capex.dom.order.approval.OrderApprovalState;
import org.estatio.module.capex.dom.project.Project;
import org.estatio.module.charge.dom.Charge;
import org.estatio.module.party.dom.Organisation;
import org.estatio.module.party.dom.Party;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderRepository_Test {

    private Order order = new Order();

    @Test
    public void upsert_when_already_exists() throws Exception {

        // given
        OrderRepository orderRepository = new OrderRepository() {
            @Override
            public Order findByOrderNumber(final String orderNumber) {
                return order;
            }
        };
        String number = "some number";
        String sellerOrderReference = "ref";
        LocalDate entryDate = new LocalDate(2017, 1, 1);
        LocalDate orderDate = new LocalDate(2017, 1, 2);
        Party seller = new Organisation();
        Party buyer = new Organisation();
        Property property = new Property();
        String atPath = "atPath";
        OrderApprovalState approvalState = OrderApprovalState.APPROVED;

        assertThat(order.getOrderNumber()).isNull();

        // when
        orderRepository.upsert(
                property,
                null,
                number,
                sellerOrderReference,
                entryDate,
                orderDate,
                seller,
                buyer,
                atPath,
                approvalState);

        // then
        assertThat(order.getOrderNumber()).isNull();
        assertThat(order.getSellerOrderReference()).isEqualTo(sellerOrderReference);
        assertThat(order.getEntryDate()).isEqualTo(entryDate);
        assertThat(order.getOrderDate()).isEqualTo(orderDate);
        assertThat(order.getSeller()).isEqualTo(seller);
        assertThat(order.getBuyer()).isEqualTo(buyer);
        assertThat(order.getProperty()).isEqualTo(property);
        assertThat(order.getAtPath()).isEqualTo(atPath);
        assertThat(order.getApprovalState()).isNull(); // is ignored.

    }

    @Test
    public void strip_ita_references_for_order_number() throws Exception {
        // given
        final String nextIncrement = "0005";
        final Property property = new Property();
        property.setReference("RON");
        final Project project = new Project();
        project.setReference("ITPR001");
        final Charge charge = new Charge();
        charge.setReference("ITWT002");

        // when
        final String orderNumber = OrderRepository.toItaOrderNumber(nextIncrement, property, null, project, charge);

        // then
        assertThat(orderNumber).isEqualTo("0005/RON/001/002");
    }

    @Test
    public void to_ita_ordernumber_works(){


        // given
        OrderRepository orderRepository = new OrderRepository();
        String nextIncrement = "1234";

        // when
        Property property = new Property();
        property.setReference("OXF");
        Project project = new Project();
        project.setReference("PR123");
        Charge charge = new Charge();
        charge.setReference("N005");

        // then
        assertThat(orderRepository.toItaOrderNumber(nextIncrement, property, null, project, charge)).isEqualTo("1234/OXF/123/005");
        assertThat(orderRepository.toItaOrderNumber(nextIncrement, property, null, null, charge)).isEqualTo("1234/OXF//005");
        assertThat(orderRepository.toItaOrderNumber(nextIncrement, property, null, project, null)).isEqualTo("1234/OXF/123/");
        assertThat(orderRepository.toItaOrderNumber(nextIncrement, property, null, null, null)).isEqualTo("1234/OXF//");

        // when
        String multiPropertyRef = "GEN";
        assertThat(orderRepository.toItaOrderNumber(nextIncrement, null, multiPropertyRef, project, charge)).isEqualTo("1234/GEN/123/005");

    }

}