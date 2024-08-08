package gift.repository.product.option;


import gift.domain.product.Product;
import gift.domain.product.option.ProductOption;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {
    @EntityGraph(attributePaths = {"product", "product.category"})
    List<ProductOption> findByProductId(Long productId);

    boolean existsByProductAndName(Product product, String name);

    @Query("SELECT p FROM Product p JOIN FETCH p.options WHERE p.id = :productId")
    Product findByIdWithOptions(Long productId);

    Optional<ProductOption> findByProductIdAndName(Long productId, String name);

}


