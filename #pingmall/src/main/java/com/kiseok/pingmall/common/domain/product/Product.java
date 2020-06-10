package com.kiseok.pingmall.common.domain.product;

import com.kiseok.pingmall.common.domain.account.Account;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity @Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String size;

    @Column
    private String image;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    @Column
    private LocalDateTime registeredAt;

    @ManyToOne
    private Account buyer;

    @ManyToOne
    private Account seller;
}
