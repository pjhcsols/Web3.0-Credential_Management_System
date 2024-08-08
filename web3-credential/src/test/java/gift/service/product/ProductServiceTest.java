package gift.service.product;

import gift.domain.category.Category;
import gift.domain.product.Product;
import gift.repository.category.CategoryRepository;
import gift.repository.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("모든 제품을 조회할 수 있어야 한다")
    void getAllProducts() {
        // 테스트용 제품 리스트 생성
        List<Product> productList = new ArrayList<>();
        productList.add(new Product("Product 1", 1000L, "Description 1", "image1.jpg", null));
        productList.add(new Product("Product 2", 2000L, "Description 2", "image2.jpg", null));
        Page<Product> page = new PageImpl<>(productList);

        // Mock 객체 사용하여 카테고리 생성
        Long categoryId = 1L;
        Category category = mock(Category.class); // Mock 객체로 대체
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.findAllByCategory(eq(category), any(PageRequest.class))).thenReturn(page);

        // 서비스 메서드 호출
        Page<Product> products = productService.getAllProducts(PageRequest.of(0, 10), categoryId);

        // 검증
        assertEquals(2, products.getTotalElements());
        assertEquals("Product 1", products.getContent().get(0).getName());
        assertEquals("Product 2", products.getContent().get(1).getName());
    }


    @Test
    @DisplayName("존재하지 않는 제품 ID로 조회 시 예외가 발생해야 한다")
    void getProductById_ProductNotFound() {
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> productService.getProductById(productId));
    }

    @Test
    @DisplayName("제품을 정상적으로 추가할 수 있어야 한다")
    void addProduct() {
        Product product = new Product("New Product", 1500L, "New Description", "new_image.jpg", null);
        Long categoryId = 1L;
        Category category = mock(Category.class);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        productService.addProduct(product, categoryId);

        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("유효하지 않은 제품 이름으로 추가 시 예외가 발생해야 한다")
    void addProduct_InvalidProductName() {
        Product product = new Product(null, 1500L, "New Description", "new_image.jpg", null);
        Long categoryId = 1L;
        assertThrows(IllegalArgumentException.class, () -> productService.addProduct(product, categoryId));
    }


    @Test
    @DisplayName("제품을 정상적으로 업데이트할 수 있어야 한다")
    void updateProduct() {
        Long productId = 1L;
        Long categoryId = 2L;

        // 기존 제품 설정
        Product existingProduct = new Product("Old Product", 1000L, "Old Description", "old_image.jpg", null);
        // Set the ID directly on the existing product
        setField(existingProduct, "id", productId);

        // 업데이트할 제품 설정
        Product productToUpdate = new Product("Updated Product", 2000L, "Updated Description", "updated_image.jpg", null);
        // Set the ID on the product to update
        setField(productToUpdate, "id", productId);

        // Mocking
        Category category = mock(Category.class);
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // Update product
        productService.updateProduct(productToUpdate, categoryId);

        // Validate updates
        assertEquals("Updated Product", existingProduct.getName());
        assertEquals(2000L, existingProduct.getPrice());
        assertEquals("Updated Description", existingProduct.getDescription());
        assertEquals("updated_image.jpg", existingProduct.getImageUrl());
        assertEquals(category, existingProduct.getCategory());

        // Verify that the productRepository save method is not called explicitly
        // Because transaction management should handle saving
        verify(productRepository, never()).save(any(Product.class));
    }

    private void setField(Object obj, String fieldName, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set field value", e);
        }
    }


    @Test
    @DisplayName("유효하지 않은 제품 이름으로 업데이트 시 예외가 발생해야 한다")
    void updateProduct_InvalidProductName() {
        Product product = new Product(null, 2000L, "Updated Description", "updated_image.jpg", null);
        Long categoryId = 1L;
        assertThrows(IllegalArgumentException.class, () -> productService.updateProduct(product, categoryId));
    }

    @Test
    @DisplayName("제품을 정상적으로 삭제할 수 있어야 한다")
    void deleteProduct() {
        Long productId = 1L;
        productService.deleteProduct(productId);
        verify(productRepository, times(1)).deleteById(productId);
    }
}
