package gift.service.product.option;

import gift.domain.category.Category;
import gift.domain.product.Product;
import gift.domain.product.option.ProductOption;
import gift.repository.product.ProductRepository;
import gift.repository.product.option.ProductOptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductOptionServiceTest {

    @Mock
    private ProductOptionRepository productOptionRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductOptionService productOptionService;

    private Product product;
    private ProductOption productOption;
    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category("교환권", "#FF5733", "https://example.com/category1.jpg", "샘플 카테고리 설명");
        product = new Product("샘플 상품 1", 10000L, "샘플 상품 설명 1", "c3", category);
        productOption = new ProductOption("테스트 옵션", 100L, product);
    }

    @Test
    void 옵션_수량_차감_성공() {
        // Arrange
        when(productOptionRepository.findByProductIdAndName(anyLong(), anyString())).thenReturn(Optional.of(productOption));

        // Act
        productOptionService.subtractProductOptionQuantity(1L, "테스트 옵션", 10L);

        // Assert
        assertEquals(90L, productOption.getQuantity());
        verify(productOptionRepository, times(1)).save(productOption);
    }

    @Test
    void 옵션_수량_부족_예외() {

        when(productOptionRepository.findByProductIdAndName(anyLong(), anyString())).thenReturn(Optional.of(productOption));


        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                productOptionService.subtractProductOptionQuantity(1L, "테스트 옵션", 200L));
        assertEquals("수량이 부족합니다", exception.getMessage());
    }


    @Test
    void 음수_수량_예외() {

        when(productOptionRepository.findByProductIdAndName(anyLong(), anyString())).thenReturn(Optional.of(productOption));


        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                productOptionService.subtractProductOptionQuantity(1L, "테스트 옵션", -10L));
        assertEquals("차감할 수량은 양수여야 합니다", exception.getMessage());
    }

    @Test
    void 제로_수량_예외() {

        when(productOptionRepository.findByProductIdAndName(anyLong(), anyString())).thenReturn(Optional.of(productOption));


        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                productOptionService.subtractProductOptionQuantity(1L, "테스트 옵션", 0L));
        assertEquals("차감할 수량은 양수여야 합니다", exception.getMessage());
    }
}
