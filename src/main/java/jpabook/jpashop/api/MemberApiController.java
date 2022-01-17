package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

@RestController
@RequiredArgsConstructor
// @ResponseBody - json이나 xml로 바로 보내자
public class MemberApiController {

    private final MemberService memberService;

    // Entity 바로 받는 것 수정할 예정
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);
        // 기본 에러 반환
        // @Valid에서 값을 검사(@NotEmpty) -> Presentation 계층에서의 검증 로직이 Entity에 모두 들어가있음.
        // 어떤 api에서는 name의 NotEmpty가 필요하지 않을 수도 있다.
        // Entity에서 name을 username으로 바꾸는 경우 [api 스펙](1:1 매칭) 자체가 바뀌어버림 가장 큰 문제! -> 별도 dto 필요
        return new CreateMemberResponse(id);
    }

    // Entity가 변경되어도 api 스펙에는 전혀 영향이 없음. 컴파일 에러로 처리 가능.
    // dto로 api 스펙을 파악 가능.
    // 실무에서는 외부에 Entity 노출, 직접 파라미터로 그대로 받는 것은 절대 X -> 장애의 원인, side effect
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @Data
    static class CreateMemberRequest {
        // 각 api별로 필요한 validation을 설정할 수 있다.
        @NotEmpty
        private String name;
    }

    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }
}