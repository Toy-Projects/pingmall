package com.kiseok.pingmall.common.domain.verification;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.kiseok.pingmall.common.domain.BaseTimeEntity;
import lombok.*;
import javax.persistence.*;

@ToString
@Getter @Builder
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity @Table
@JsonIdentityInfo(
        scope = Verification.class,
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
public class Verification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column
    private String verificationCode;

    @Column
    private Boolean isVerified = false;

    public void verified() {
        this.isVerified = !isVerified;
    }
}
