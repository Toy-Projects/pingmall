package com.kiseok.pingmall.common.domain.order;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.product.Product;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity @Table
@EqualsAndHashCode(of = "id")
@JsonIdentityInfo(
        scope = Orders.class,
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String number;

    @Column
    private Long amount;

    @Column
    private LocalDateTime orderedAt;

    @ManyToOne
    private Account buyer;

    @ManyToOne
    private Product product;
}
