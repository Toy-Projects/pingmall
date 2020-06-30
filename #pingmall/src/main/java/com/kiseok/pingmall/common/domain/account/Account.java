package com.kiseok.pingmall.common.domain.account;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.kiseok.pingmall.api.exception.account.BalanceShortageException;
import com.kiseok.pingmall.common.domain.order.Orders;
import com.kiseok.pingmall.common.domain.product.Product;
import com.kiseok.pingmall.web.dto.account.AccountDepositRequestDto;
import com.kiseok.pingmall.web.dto.account.AccountModifyRequestDto;
import com.kiseok.pingmall.web.dto.order.OrdersRequestDto;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Getter @Builder
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(of = "id")
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

    @JsonIgnore
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

    @JsonIgnore
    @OneToMany(mappedBy = "buyer", fetch = FetchType.EAGER)
    private Set<Orders> orders;

    @JsonIgnore
    @OneToMany(mappedBy = "seller", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Product> sellProducts;

    public void addBalance(AccountDepositRequestDto requestDto) {
        this.balance += requestDto.getBalance();
    }

    public void reduceBalance(OrdersRequestDto requestDto, Long price) {
        long payment = requestDto.getAmount() * price;
        if(this.balance < payment || this.balance <= 0L)    {
            throw new BalanceShortageException();
        }
        this.balance -= payment;
    }

    public void updateAccount(AccountModifyRequestDto requestDto) {
        this.password = requestDto.getPassword();
        this.name = requestDto.getName();
        this.address = requestDto.getAddress();
    }
}
