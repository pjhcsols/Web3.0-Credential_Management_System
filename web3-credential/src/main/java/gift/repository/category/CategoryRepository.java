package gift.repository.category;

import gift.domain.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Modifying
    @Query("UPDATE Category c SET c.name = :name, c.color = :color, c.imageUrl = :imageUrl, c.description = :description WHERE c.id = :id")
    void updateCategory(@Param("id") Long id, @Param("name") String name, @Param("color") String color, @Param("imageUrl") String imageUrl, @Param("description") String description);
}
