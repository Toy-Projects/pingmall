package com.kiseok.pingmall.common.domain.account;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);
    Optional<Account> findByName(String name);
    Optional<Account> findByEmailAndName(String email, String name);

}
