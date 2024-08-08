package gift.service.product;

import gift.domain.category.Category;
import gift.domain.product.Product;
import gift.repository.category.CategoryRepository;
import gift.repository.product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public Page<Product> getAllProducts(Pageable pageable, Long categoryId) {
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));
            return productRepository.findAllByCategory(category, pageable);
        }
        return productRepository.findAll(pageable);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
    }

    @Transactional
    public void addProduct(Product product, Long categoryId) {
        validateProduct(product);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));
        product.saveCategory(category);
        productRepository.save(product);
    }

    @Transactional
    public void updateProduct(Product product, Long categoryId) {
        Product existingProduct = productRepository.findById(product.getId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + product.getId()));

        // 엔티티의 필드를 업데이트
        existingProduct.update(product.getName(), product.getPrice(), product.getDescription(), product.getImageUrl());

        // 엔티티의 추가 카테고리 업데이트
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));
        existingProduct.saveCategory(category);

        // 엔티티는 이미 트랜잭션에 의해 관리되므로, 별도의 `save` 호출이 필요하지 않습니다.
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    private void validateProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product must not be null");
        }
        if (product.getName() == null || product.getName().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (product.getPrice() == null || product.getPrice() <= 0) {
            throw new IllegalArgumentException("Product price must be greater than 0");
        }
        if (product.getImageUrl() == null || product.getImageUrl().isEmpty()) {
            throw new IllegalArgumentException("Product image URL is required");
        }
    }
}
