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
        changeOrderTableTableGroup(orderTables);
        this.id = id;
        this.createdDate = LocalDateTime.now();
        this.orderTables = orderTables;
    }

    public void ungroup() {
        orderTables.ungroup();
    }

    private void changeOrderTableTableGroup(final OrderTables orderTables) {
        orderTables.getOrderTables().forEach(orderTable -> orderTable.changeTableGroup(this));
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
