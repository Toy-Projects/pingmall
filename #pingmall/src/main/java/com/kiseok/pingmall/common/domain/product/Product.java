package com.kiseok.pingmall.common.domain.product;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.kiseok.pingmall.api.exception.product.StockShortageException;
import com.kiseok.pingmall.common.domain.BaseTimeEntity;
import com.kiseok.pingmall.common.domain.account.Account;
import com.kiseok.pingmall.common.domain.comment.Comment;
import com.kiseok.pingmall.common.domain.order.Orders;
import com.kiseok.pingmall.web.dto.order.OrdersRequestDto;
import com.kiseok.pingmall.web.dto.product.ProductRequestDto;
import lombok.*;
import javax.persistence.*;
import java.util.Set;

@Getter @Builder
@AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity @Table
@JsonIdentityInfo(
        scope = Product.class,
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
public class Product extends BaseTimeEntity {

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
    private Long price;

    @Column(nullable = false)
    private Long stock;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    @OneToMany(mappedBy = "product", fetch = FetchType.EAGER)
    private Set<Orders> orders;

    @ManyToOne
    private Account seller;

    @OneToMany(mappedBy = "product", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    private Set<Comment> comment;

    public void reduceStock(OrdersRequestDto requestDto) {
        if(this.stock < requestDto.getAmount() || this.stock <= 0L) {
            throw new StockShortageException();
        }
        this.stock -= requestDto.getAmount();
    }

    public void uploadImage(String imagePath) {
        this.image = imagePath;
    }

    public void updateProduct(ProductRequestDto requestDto) {
        this.name = requestDto.getName();
        this.size = requestDto.getSize();
        this.image = requestDto.getImage();
        this.price = requestDto.getPrice();
        this.stock = requestDto.getStock();
        this.category = requestDto.getCategory();
    }
}
