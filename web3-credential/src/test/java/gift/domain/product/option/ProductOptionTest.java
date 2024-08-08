package gift.domain.product.option;

import gift.domain.product.Product;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ProductOptionTest {

    @Test
    public void testSubtractValidQuantity() {
        Product product = new Product("Test Product", 1000L, "Test Description", "test.png");
        ProductOption option = new ProductOption("Option 1", 10L, product);

        option.subtract(5L);

        assertEquals(5L, option.getQuantity());
    }

    @Test
    public void testSubtractInvalidQuantity() {
        Product product = new Product("Test Product", 1000L, "Test Description", "test.png");
        ProductOption option = new ProductOption("Option 1", 10L, product);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            option.subtract(-5L);
        });

        assertEquals("차감할 수량은 양수여야 합니다", thrown.getMessage());
    }

    @Test
    public void testSubtractMoreThanAvailableQuantity() {
        Product product = new Product("Test Product", 1000L, "Test Description", "test.png");
        ProductOption option = new ProductOption("Option 1", 10L, product);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            option.subtract(15L);
        });

        assertEquals("수량이 부족합니다", thrown.getMessage());
    }
}
