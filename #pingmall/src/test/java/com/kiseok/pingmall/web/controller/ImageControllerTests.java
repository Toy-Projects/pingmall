package com.kiseok.pingmall.web.controller;

import com.kiseok.pingmall.web.common.BaseControllerTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class ImageControllerTests extends BaseControllerTest {

    @AfterEach
    void deleteAll()    {
        productRepository.deleteAll();
        accountRepository.deleteAll();
    }

    // TODO DB에 없는 제품의 이미지 업로드 시 -> 404 NOT_FOUND

    // TODO 파일 이름에 '..' 이 있을 경우 -> 400 BAD_REQUEST

    // TODO 이미지가 아닌 파일 저장 시 -> 400 BAD_REQUEST

    // TODO 정상적으로 이미지 저장 -> 200 OK

    // TODO DB에 없는 제품의 디폴트 이미지 업로드 시 -> 404 NOT_FOUND

    // TODO 정상적으로 디폴트 이미지 저장 -> 200 OK

    // TODO DB에 없는 제품의 이미지 불러올 시 -> 404 NOT_FOUND

    // TODO 정상적으로 이미지 불러오기 -> 200 OK
}