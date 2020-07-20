package com.kiseok.pingmall.common.domain.product;

import com.kiseok.pingmall.web.dto.product.ProductFilterRequestDto;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import static com.kiseok.pingmall.common.domain.product.QProduct.product;

@Repository
public class ProductRepositoryImpl extends QuerydslRepositorySupport implements ProductRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    public ProductRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        super(Product.class);
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    public Page<Product> findByFilter(ProductFilterRequestDto requestDto, Pageable pageable)   {
        QueryResults<Product> filteredProducts =
        jpaQueryFactory.selectFrom(QProduct.product)
                .where(
                        isContainName(requestDto.getName()),
                        isContainSize(requestDto.getSize()),
                        isLessThanPrice(requestDto.getPrice()),
                        isContainsCategory(requestDto.getCategory())
                )
                .orderBy(isContainOrderBy(requestDto.getOrderBy()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults()
        ;

        return new PageImpl<>(filteredProducts.getResults(), pageable, filteredProducts.getTotal());
    }

    private BooleanExpression isContainName(String name) {
        if(StringUtils.isEmpty(name))   {
            return null;
        }
        return product.name.contains(name);
    }

    private BooleanExpression isContainSize(String size) {
        if(StringUtils.isEmpty(size))   {
            return null;
        }
        return product.size.eq(size);
    }

    private BooleanExpression isLessThanPrice(String price) {
        if(StringUtils.isEmpty(price))  {
            return null;
        }
        long priceChanger = Long.parseLong(price);
        return product.price.loe(priceChanger);
    }

    private BooleanExpression isContainsCategory(ProductCategory category) {
        if(category == null)    {
            return null;
        }
        return product.category.stringValue().eq(category.name());
    }

    private OrderSpecifier<?> isContainOrderBy(String orderBy) {
        if("name".equals(orderBy))  {
            return product.name.asc();
        }
        else if("size".equals(orderBy)) {
            return product.size.asc();
        }
        else if("price".equals(orderBy))    {
            return product.price.asc();
        }
        else if("category".equals(orderBy)) {
            return product.category.asc();
        }
        return product.createdAt.asc();
    }

}
