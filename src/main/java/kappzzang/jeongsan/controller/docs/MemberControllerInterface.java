package kappzzang.jeongsan.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kappzzang.jeongsan.dto.request.LoginRequest;
import kappzzang.jeongsan.dto.request.RefreshRequest;
import kappzzang.jeongsan.dto.request.RegisterRequest;
import kappzzang.jeongsan.dto.response.GetPayLinkResponse;
import kappzzang.jeongsan.dto.response.LoginResponse;
import kappzzang.jeongsan.dto.response.RefreshResponse;
import kappzzang.jeongsan.global.common.ApiErrorTypeExample;
import kappzzang.jeongsan.global.common.JeongsanApiResponse;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import org.springframework.http.ResponseEntity;

@Tag(name = "회원 관리", description = "JWT 반환, 멤버 그룹 참여, 초대 현황 등을 관리하는 API")
public interface MemberControllerInterface {

    @Operation(summary = "로그인 API", description = "카카오 로그인으로 토큰을 받는 API")
    @Parameters({
        @Parameter(name = "email", description = "카카오 회원 정보의 이메일")
    })
    @ApiResponse(responseCode = "200", description = "로그인 성공")
    @ApiErrorTypeExample(ErrorType.USER_NOT_FOUND)
    ResponseEntity<JeongsanApiResponse<LoginResponse>> login(LoginRequest loginRequest);

    @Operation(summary = "회원가입 API", description = "카카오 로그인으로 회원가입 후 토큰을 받는 API")
    @Parameters({
        @Parameter(name = "nickname", description = "카카오 회원 정보의 닉네임"),
        @Parameter(name = "email", description = "카카오 회원 정보의 이메일"),
        @Parameter(name = "profileImage", description = "카카오 회원 정보의 프로필 URL")
    })
    @ApiResponse(responseCode = "201", description = "회원가입 성공")
    @ApiErrorTypeExample(ErrorType.USER_ALREADY_EXISTED)
    ResponseEntity<JeongsanApiResponse<LoginResponse>> register(RegisterRequest registerRequest);

    @Operation(summary = "액세스 토큰 재발급 API", description = "리프레시 토큰으로 액세스 토큰을 받는 API")
    @Parameters({
        @Parameter(name = "refreshToken", description = "서비스 서버 리프레시 토큰")
    })
    @ApiResponse(responseCode = "200", description = "액세스 토큰 재발급 성공")
    @ApiErrorTypeExample({ErrorType.USER_NOT_FOUND, ErrorType.REFRESH_TOKEN_INVALID})
    ResponseEntity<JeongsanApiResponse<RefreshResponse>> refresh(Long memberId,
        RefreshRequest refreshRequest);

    @Operation(summary = "모임 초대 수락 API", description = "모임 초대 링크를 받은 사용자가 모임 참여를 수락하여 모임에 참여하도록 하는 API")
    @ApiResponse(responseCode = "204", description = "모임 초대 수락, 모임 참여 성공")
    @ApiErrorTypeExample({ErrorType.NOT_INVITED_MEMBER, ErrorType.ALREADY_JOINED_MEMBER,
        ErrorType.USER_NOT_FOUND, ErrorType.TEAM_NOT_FOUND})
    ResponseEntity<JeongsanApiResponse<Void>> joinTeam(Long teamId, Long memberId);

    @Operation(summary = "송금 링크 조회 API", description = "카카오페이 송금 링크를 조회하는 API")
    @ApiResponse(responseCode = "200", description = "카카오 페이 송금 링크 조회 성공")
    @ApiErrorTypeExample(ErrorType.KAKAO_PAY_LINK_NOT_FOUND)
    ResponseEntity<JeongsanApiResponse<GetPayLinkResponse>> getPayLink(Long memberId);
}
