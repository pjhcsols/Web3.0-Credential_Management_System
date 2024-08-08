package gift.service.wish;

import gift.domain.wish.Wish;
import gift.exception.product.ProductNotFoundException;
import gift.exception.wish.WishCanNotModifyException;
import gift.exception.wish.WishNotFoundException;
import gift.repository.product.ProductRepository;
import gift.repository.wish.WishRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WishService {
    private final WishRepository wishRepository;
    private final ProductRepository productRepository;

    @Autowired
    public WishService(WishRepository wishRepository, ProductRepository productRepository) {
        this.wishRepository = wishRepository;
        this.productRepository = productRepository;
    }

    public void saveWish(Long productId, Long userId, int amount) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        Wish wish = new Wish(productId, userId, amount);
        wishRepository.save(wish);
    }

    public void modifyWish(Long wishId, Long productId, Long userId, int amount) {
        Wish wish = wishRepository.findByIdAndUserId(wishId, userId)
                .orElseThrow(() -> new WishNotFoundException(wishId));

        if (!wish.getProductId().equals(productId)) {
            throw new WishCanNotModifyException();
        }

        wish.setAmount(amount);
        wishRepository.save(wish);
    }

    public Page<Wish> getWishList(Long userId, Pageable pageable) {
        return wishRepository.findByUserIdAndIsDeletedFalse(userId, pageable);
    }

    public Wish getWishDetail(Long wishId, Long userId) {
        return wishRepository.findByIdAndUserId(wishId, userId)
                .orElseThrow(() -> new WishNotFoundException(wishId));
    }

    public void deleteWish(Long wishId, Long userId) {
        Wish wish = wishRepository.findByIdAndUserId(wishId, userId)
                .orElseThrow(() -> new WishNotFoundException(wishId));

        wish.setIsDeleted(true);
        wishRepository.save(wish);
    }
}
