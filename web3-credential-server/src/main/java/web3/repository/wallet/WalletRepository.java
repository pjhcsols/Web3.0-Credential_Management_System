package web3.repository.wallet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import web3.domain.user.User;
import web3.domain.wallet.Wallet;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    // 필요에 따라 추가적인 쿼리 메서드를 정의할 수 있습니다.
    Optional<Wallet> findByUser(User user);
}
