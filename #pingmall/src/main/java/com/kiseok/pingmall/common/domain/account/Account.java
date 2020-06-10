package com.kiseok.pingmall.common.domain.account;

import com.kiseok.pingmall.common.domain.product.Product;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder @EqualsAndHashCode(of = "id")
@Entity @Table
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
    @Enumerated(EnumType.STRING)
    private AccountRole accountRole;

    @Column
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "buyer", fetch = FetchType.EAGER)
    private Set<Product> buyProducts;

    @OneToMany(mappedBy = "seller", fetch = FetchType.EAGER)
    private Set<Product> sellProducts;
}
