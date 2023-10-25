package kitchenpos.application;

import kitchenpos.order.application.OrderService;
import kitchenpos.table.application.TableService;
import kitchenpos.tableGroup.application.TableGroupService;
import kitchenpos.product.domain.Price;
import kitchenpos.menu.domain.Menu;
import kitchenpos.menuGroup.domain.MenuGroup;
import kitchenpos.order.domain.OrderStatus;
import kitchenpos.order.dto.OrderLineItemRequest;
import kitchenpos.order.dto.OrderRequest;
import kitchenpos.order.dto.OrderStatusRequest;
import kitchenpos.table.dto.OrderTableIdRequest;
import kitchenpos.table.dto.OrderTableRequest;
import kitchenpos.tableGroup.dto.TableGroupRequest;
import kitchenpos.order.dto.OrderResponse;
import kitchenpos.table.dto.OrderTableResponse;
import kitchenpos.tableGroup.dto.TableGroupResponse;
import kitchenpos.order.exception.IllegalOrderStatusException;
import kitchenpos.table.exception.InvalidOrderTableException;
import kitchenpos.table.exception.DuplicateOrderTableException;
import kitchenpos.tableGroup.exception.TableGroupNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class TableGroupServiceTest extends ServiceBaseTest {

    @Autowired
    protected TableGroupService tableGroupService;

    @Autowired
    protected TableService tableService;

    @Autowired
    protected OrderService orderService;

    @Test
    @DisplayName("테이블 단체 지정을 할 수 있다.")
    void create() {
        //given
        final OrderTableResponse savedOrderTable = tableService.create(new OrderTableRequest(0, false));
        final OrderTableResponse savedOrderTable2 = tableService.create(new OrderTableRequest(0, false));
        final List<OrderTableIdRequest> orderTableIdRequests = List.of(new OrderTableIdRequest(savedOrderTable.getId()), new OrderTableIdRequest(savedOrderTable2.getId()));
        final TableGroupRequest tableGroupRequest = new TableGroupRequest(orderTableIdRequests);

        //when
        final TableGroupResponse tableGroupResponse = tableGroupService.create(tableGroupRequest);

        //then
        assertThat(tableGroupResponse.getId()).isNotNull();
    }

    @Test
    @DisplayName("테이블 지정은 2개 이상이어야 한다.")
    void createValidTableNum() {
        //given
        final OrderTableResponse savedOrderTable = tableService.create(new OrderTableRequest(0, false));
        final List<OrderTableIdRequest> orderTableIdRequests = List.of(new OrderTableIdRequest(savedOrderTable.getId()));
        final TableGroupRequest tableGroupRequest = new TableGroupRequest(orderTableIdRequests);

        //whdn&then
        assertThatThrownBy(() -> tableGroupService.create(tableGroupRequest))
                .isInstanceOf(InvalidOrderTableException.class)
                .hasMessage("잘못된 OrderTable 입니다.");
    }

    @Test
    @DisplayName("지정 테이블은 모두 존재하여야 한다.")
    void createValidTable() {
        //given
        tableService.create(new OrderTableRequest(0, true));
        tableService.create(new OrderTableRequest(0, true));
        final List<OrderTableIdRequest> orderTableIdRequests = List.of(new OrderTableIdRequest(999L), new OrderTableIdRequest(999L));
        final TableGroupRequest tableGroupRequest = new TableGroupRequest(orderTableIdRequests);

        //when&then
        assertThatThrownBy(() -> tableGroupService.create(tableGroupRequest))
                .isInstanceOf(InvalidOrderTableException.class)
                .hasMessage("잘못된 OrderTable 입니다.");
    }

    @Test
    @DisplayName("지정 테이블은 비어있어야 한다.")
    void createValidTableEmpty() {
        //given
        tableService.create(new OrderTableRequest(5, false));
        final OrderTableResponse savedOrderTable2 = tableService.create(new OrderTableRequest(5, false));
        final List<OrderTableIdRequest> orderTableIdRequests = List.of(new OrderTableIdRequest(999L), new OrderTableIdRequest(savedOrderTable2.getId()));
        final TableGroupRequest tableGroupRequest = new TableGroupRequest(orderTableIdRequests);

        //when&then
        assertThatThrownBy(() -> tableGroupService.create(tableGroupRequest))
                .isInstanceOf(InvalidOrderTableException.class)
                .hasMessage("잘못된 OrderTable 입니다.");
    }

    @Test
    @DisplayName("이미 지정된 테이블은 단체 지정할 수 없다.")
    void createValidAlreadyGroupTable() {
        //given
        final OrderTableResponse savedOrderTable = tableService.create(new OrderTableRequest(10, false));
        final OrderTableResponse savedOrderTable2 = tableService.create(new OrderTableRequest(5, false));
        final List<OrderTableIdRequest> orderTableIdRequests = List.of(new OrderTableIdRequest(savedOrderTable.getId()), new OrderTableIdRequest(savedOrderTable2.getId()));
        final TableGroupRequest tableGroupRequest = new TableGroupRequest(orderTableIdRequests);
        tableGroupService.create(tableGroupRequest);

        //when&then
        assertThatThrownBy(() -> tableGroupService.create(tableGroupRequest))
                .isInstanceOf(DuplicateOrderTableException.class)
                .hasMessage("이미 지정된 테이블은 단체 지정할 수 없습니다.");
    }

    @Test
    @DisplayName("테이블 단체 지정을 해제할수 있다.")
    void ungroup() {
        //given
        final OrderTableResponse savedOrderTable = tableService.create(new OrderTableRequest(5, false));
        final OrderTableResponse savedOrderTable2 = tableService.create(new OrderTableRequest(5, false));
        final List<OrderTableIdRequest> orderTableIdRequests = List.of(new OrderTableIdRequest(savedOrderTable.getId()), new OrderTableIdRequest(savedOrderTable2.getId()));
        final TableGroupRequest tableGroupRequest = new TableGroupRequest(orderTableIdRequests);
        final TableGroupResponse tableGroupResponse = tableGroupService.create(tableGroupRequest);

        //when&then
        assertDoesNotThrow(() -> tableGroupService.ungroup(tableGroupResponse.getId()));
    }

    @Test
    @DisplayName("TableGroup이 없을 경우 해제할 수 없다.")
    void ungroupValidTableGroup() {
        //when&then
        assertThatThrownBy(() -> tableGroupService.ungroup(999L))
                .isInstanceOf(TableGroupNotFoundException.class)
                .hasMessage("TableGroup을 찾을 수 없습니다.");
    }

    @ParameterizedTest(name = "테이블 단체 지정을 해제시 상태가 요리, 식사이면 안된다.")
    @EnumSource(value = OrderStatus.class, names = {"COOKING", "MEAL"})
    void ungroupValidTable(final OrderStatus orderStatus) {
        //given
        final OrderTableResponse savedOrderTable = tableService.create(new OrderTableRequest(5, false));
        final OrderTableResponse savedOrderTable2 = tableService.create(new OrderTableRequest(5, false));
        final TableGroupResponse tableGroupResponse = tableGroupService.create(new TableGroupRequest(List.of(new OrderTableIdRequest(savedOrderTable.getId()),
                new OrderTableIdRequest(savedOrderTable2.getId()))));
        final MenuGroup menuGroup = menuGroupRepository.save(new MenuGroup("메뉴 그룹"));
        final Menu menu = menuRepository.save(new Menu("메뉴1", new Price(new BigDecimal(1000)), menuGroup.getId(), null));
        final OrderResponse orderResponse = orderService.create(new OrderRequest(savedOrderTable.getId(), List.of(new OrderLineItemRequest(menu.getId(), 3L))));

        orderService.changeOrderStatus(orderResponse.getId(), new OrderStatusRequest(orderStatus));

        //when&then
        assertThatThrownBy(() -> tableGroupService.ungroup(tableGroupResponse.getId()))
                .isInstanceOf(IllegalOrderStatusException.class)
                .hasMessage("잘못된 주문 상태입니다.");
    }
}
