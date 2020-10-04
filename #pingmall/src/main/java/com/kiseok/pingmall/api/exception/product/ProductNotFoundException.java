package com.kiseok.pingmall.api.exception.product;

/**
 * 제품이 가지고 있는 고유한 ID를 가지고 특정 행위를 할 때, Database에 없는 ID로 접근하였을 때 발생하는 Exception
 *
 * @author kiseokyang
 */

public class ProductNotFoundException extends RuntimeException{
}
