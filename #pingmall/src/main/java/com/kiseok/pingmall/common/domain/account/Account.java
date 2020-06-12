package com.kiseok.pingmall.common.domain.account;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.kiseok.pingmall.common.domain.product.Product;
import com.kiseok.pingmall.web.dto.account.AccountDepositRequestDto;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder @EqualsAndHashCode(of = "id")
@Entity @Table
@JsonIdentityInfo(
        scope = Account.class,
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)

public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column
    private Long balance;

    @Column
    @Enumerated(EnumType.STRING)
    private AccountRole accountRole;

    @Column
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "buyer", fetch = FetchType.EAGER)
    private Set<Product> buyProducts;

    @OneToMany(mappedBy = "seller", fetch = FetchType.EAGER)
    private Set<Product> sellProducts;

    public void addBalance(AccountDepositRequestDto requestDto) {
        this.balance += requestDto.getBalance();
    }
}
