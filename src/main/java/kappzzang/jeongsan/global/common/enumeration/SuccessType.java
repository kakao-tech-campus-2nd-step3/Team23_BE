package kappzzang.jeongsan.global.common.enumeration;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum SuccessType {

    // 200 OK
    TEAM_LIST_LOADED(HttpStatus.OK, "모임 목록을 불러오는 데 성공했습니다."),
    TEAM_LOADED(HttpStatus.OK, "모임을 불러오는 데 성공했습니다"),
    RECEIPT_ANALYSIS_SUCCESS(HttpStatus.OK, "영수증 분석을 성공하였습니다."),
    EXPENSE_LIST_LOADED(HttpStatus.OK, "지출 목록을 불러오는 데 성공했습니다"),
    PERSONAL_EXPENSE_LOADED(HttpStatus.OK, "개인 지출 목록을 불러오는 데 성공했습니다."),
    INVITATION_STATUS_LOADED(HttpStatus.OK, "모임의 멤버 초대 현황을 불러오는 데 성공했습니다."),
    LOGGED_IN(HttpStatus.CREATED, "로그인을 성공하였습니다."),
    ACCESS_TOKEN_REISSUED(HttpStatus.OK, "액세스 토큰이 재발급되었습니다."),
    PAY_LINK_LOADED(HttpStatus.OK, "카카오 페이 송금 링크 조회 성공"),
    PERSONAL_EXPENSE_SAVED(HttpStatus.OK, "개인 소비 내역이 저장되었습니다."),
    TRANSFER_TARGET_LIST_LOADED(HttpStatus.OK, "송금 요청 대상과 금액 목록을 불러오는 데 성공했습니다."),
    CATEGORY_LIST_LOADED(HttpStatus.OK, "카테고리 목록을 불러오는 데 성공했습니다."),
    EXPENSE_DETAIL_LOADED(HttpStatus.OK, "지출 선택 상세를 불러오는 데 성공했습니다."),
    MEMBER_ID_LOADED(HttpStatus.OK, "모임의 멤버 아이디를 불러오는 데 성공했습니다."),

    // 201 CREATED
    SIGNED_UP(HttpStatus.CREATED, "회원가입을 성공하였습니다."),
    TEAM_CREATED(HttpStatus.CREATED, "모임이 생성되었습니다."),
    EXPENSE_CREATED(HttpStatus.CREATED, "지출 내역 등록을 성공하였습니다."),

    //204 NO_CONTENT
    TEAM_CLOSED(HttpStatus.NO_CONTENT, "모임이 종료되었습니다."),
    JOIN_SUCCESS(HttpStatus.NO_CONTENT, "모임 멤버로 참여되었습니다."),
    EXPENSE_STATUS_CHANGE_SUCCESS(HttpStatus.NO_CONTENT, "지출 상태 변경을 완료하였습니다.");

    private final HttpStatusCode httpStatusCode;
    private final String message;

    SuccessType(HttpStatusCode httpStatusCode, String message) {
        this.httpStatusCode = httpStatusCode;
        this.message = message;
    }
}
