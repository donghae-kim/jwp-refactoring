package kitchenpos.domain;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class TableGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdDate;

    @Embedded
    private OrderTables orderTables;

    protected TableGroup() {
    }

    public TableGroup(final OrderTables orderTables) {
        this(null, orderTables);
    }

    public TableGroup(final Long id, final OrderTables orderTables) {
        changeOrderTableTableGroup(orderTables, this);
        this.id = id;
        this.createdDate = LocalDateTime.now();
        this.orderTables = orderTables;
    }

    public void ungroup() {
        changeOrderTableTableGroup(this.orderTables, null);
    }

    private void changeOrderTableTableGroup(final OrderTables orderTables, final TableGroup tableGroup) {
        orderTables.getOrderTables().stream().forEach(orderTable -> orderTable.changeTableGroup(tableGroup));
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public List<OrderTable> getOrderTables() {
        return orderTables.getOrderTables();
    }
}
