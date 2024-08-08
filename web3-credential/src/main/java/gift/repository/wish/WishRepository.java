package gift.repository.wish;

import gift.domain.wish.Wish;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface WishRepository extends JpaRepository<Wish, Long> {

    Optional<Wish> findByIdAndUserId(Long wishId, Long userId);

    Page<Wish> findByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);

    Optional<Wish> findByUserIdAndProductIdAndIsDeletedFalse(Long userId, Long productId);
}
