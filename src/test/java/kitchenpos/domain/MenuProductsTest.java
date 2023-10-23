package kitchenpos.domain;

import kitchenpos.exception.menuProductException.InvalidMenuProductsPriceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MenuProductsTest {
    @Test
    void create() {
        //given
        final MenuProduct menuProduct1 = new MenuProduct(new Product("이름", new Price(new BigDecimal(1000))), 1);
        final MenuProduct menuProduct2 = new MenuProduct(new Product("이름2", new Price(new BigDecimal(1000))), 1);

        //when && then
        Assertions.assertDoesNotThrow(() -> new MenuProducts(List.of(menuProduct1, menuProduct2), new BigDecimal(1000)));
    }

    @Test
    void validateSum() {
        //given
        final MenuProduct menuProduct1 = new MenuProduct(new Product("이름", new Price(new BigDecimal(1000))), 1);
        final MenuProduct menuProduct2 = new MenuProduct(new Product("이름2", new Price(new BigDecimal(1000))), 1);

        //when && then
        assertThatThrownBy(() -> new MenuProducts(List.of(menuProduct1, menuProduct2), new BigDecimal(10000)))
                .isInstanceOf(InvalidMenuProductsPriceException.class)
                .hasMessage("메뉴의 가격은 메뉴 상품 가격 합 이하여야 합니다.");
    }
}
