package gift.service.product.option;

import gift.domain.product.Product;
import gift.domain.product.option.ProductOption;
import gift.repository.product.ProductRepository;
import gift.repository.product.option.ProductOptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductOptionService {

    private final ProductOptionRepository productOptionRepository;
    private final ProductRepository productRepository;

    @Autowired
    public ProductOptionService(ProductOptionRepository productOptionRepository, ProductRepository productRepository) {
        this.productOptionRepository = productOptionRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductOption addProductOption(Long productId, String name, Long quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (productOptionRepository.existsByProductAndName(product, name)) {
            throw new IllegalArgumentException("Option name already exists for this product");
        }

        ProductOption option = new ProductOption(name, quantity, product);
        return productOptionRepository.save(option);
    }

    public List<ProductOption> getProductOptions(Long productId) {
        return productOptionRepository.findByProductId(productId);
    }

    @Transactional
    public void subtractProductOptionQuantity(Long productId, String optionName, Long quantity) {
        ProductOption option = productOptionRepository.findByProductIdAndName(productId, optionName)
                .orElseThrow(() -> new IllegalArgumentException("Option not found"));
        option.subtract(quantity);
        productOptionRepository.save(option);
    }

    @Transactional
    public ProductOption updateProductOption(Long productId, Long optionId, String name, Long quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        ProductOption option = productOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("Option not found"));

        if (!option.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException("Option does not belong to the specified product");
        }

        option = option.withUpdatedNameAndQuantity(name, quantity);
        return productOptionRepository.save(option);
    }

    @Transactional
    public void deleteProductOption(Long productId, Long optionId) {
        ProductOption option = productOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("Option not found"));

        if (!option.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException("Option does not belong to the specified product");
        }

        productOptionRepository.delete(option);
    }

}
