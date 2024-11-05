package kappzzang.jeongsan.global.common.enumeration;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorType {

    // 400 BAD_REQUEST
    TEAM_ALREADY_CLOSED(HttpStatus.BAD_REQUEST, "E400001", "이미 종료된 모임입니다."),
    EXPENSE_MISSING_PARAM(HttpStatus.BAD_REQUEST, "E400002", "누락된 쿼리 파라미터가 존재합니다."),
    EXPENSE_INVALID_STATE(HttpStatus.BAD_REQUEST, "E400003", "잘못된 state 값 요청입니다."),
    NOT_INVITED_MEMBER(HttpStatus.BAD_REQUEST, "E400004", "해당 모임에 초대되지 않은 멤버입니다."),
    ALREADY_JOINED_MEMBER(HttpStatus.BAD_REQUEST, "E400005", "이미 모임에 참여한 멤버입니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "E400006", "요청 입력값이 유효하지 않습니다."),
    EXPENSE_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "E400007", "이미 정산 완료된 지출입니다."),
    EXPENSE_ONGOING(HttpStatus.BAD_REQUEST, "E400008", "아직 진행중인 지출입니다."),
    EXPENSE_NOT_FOUND_ID(HttpStatus.BAD_REQUEST, "E400009", "요청 목록에 존재하지 않는 지출이 포함되어 있습니다."),
    EXPENSE_INVALID_TEAM(HttpStatus.BAD_REQUEST, "E400010", "요청 목록에 타 모임의 지출이 포함되어 있습니다."),
    EXPENSE_INVALID_PAYER(HttpStatus.BAD_REQUEST, "E400011", "요청 목록에 본인이 결제하지 않은 지출이 포함되어 있습니다."),
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "E400012", "잘못된 quantity 값 요청입니다."),
    ALREADY_CHECKED_ITEM(HttpStatus.BAD_REQUEST, "E400013", "이미 선택 완료 한 품목이 포함된 요청입니다."),
    EXPENSE_NOT_IN_TEAM(HttpStatus.BAD_REQUEST, "E400014", "팀에 속하지 않은 지출에 대한 요청입니다."),

    // 401 UNAUTHORIZED
    JWT_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "E401001", "토큰 서명이 유효하지 않습니다."),
    JWT_EXPIRED(HttpStatus.UNAUTHORIZED, "E401002", "토큰이 만료되었습니다."),
    JWT_MALFORMED(HttpStatus.UNAUTHORIZED, "E401003", "토큰 형식이 잘못되었습니다."),

    // 403 FORBIDDEN
    REFRESH_TOKEN_INVALID(HttpStatus.FORBIDDEN, "E403001", "리프레시 토큰이 유효하지 않습니다."),

    // 404 NOT_FOUND
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "E404001", "사용자를 찾을 수 없습니다."),
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "E404002", "모임을 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "E404003", "카테고리을 찾을 수 없습니다."),
    EXPENSE_NOT_FOUND(HttpStatus.NOT_FOUND, "E404004", "지출을 찾을 수 없습니다."),
    INVITATION_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "E404005", "초대 현황을 찾을 수 없습니다."),
    KAKAO_PAY_LINK_NOT_FOUND(HttpStatus.NOT_FOUND, "E404006", "카카오 페이 송금 링크를 찾을 수 없습니다."),
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "E404007", "아이템(영수증 품목)을 찾을 수 없습니다."),
    TEAM_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "E404008", "팀 멤버를 찾을 수 없습니다."),
    PERSONAL_EXPENSE_NOT_FOUND(HttpStatus.NOT_FOUND, "E404", "개인 소비 내역을 찾을 수 없습니다."),



    // 408 REQUEST_TIMEOUT
    EXTERNAL_API_REQUEST_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "E408001", "외부 API 요청 시간이 초과되었습니다."),

    // 409 CONFLICT
    TEAM_NAME_DUPLICATED(HttpStatus.CONFLICT, "E409001", "중복된 모임 이름이 존재합니다."),
    USER_ALREADY_EXISTED(HttpStatus.CONFLICT, "E409002", "이미 회원가입된 사용자입니다."),

    // 500 INTERNAL_SERVER_ERROR
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E500001", "서버 내부 오류가 발생했습니다."),
    RECEIPT_EXTRACTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "E500002", "영수증 데이터 추출에 실패했습니다."),
    EXTERNAL_API_GENERAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E500003",
        "외부 API 호출 중 오류가 발생하였습니다.");

    private final HttpStatusCode httpStatusCode;
    private final String errorCode;
    private final String message;

    ErrorType(HttpStatusCode httpStatusCode, String errorCode, String message) {
        this.httpStatusCode = httpStatusCode;
        this.errorCode = errorCode;
        this.message = message;
    }
}
