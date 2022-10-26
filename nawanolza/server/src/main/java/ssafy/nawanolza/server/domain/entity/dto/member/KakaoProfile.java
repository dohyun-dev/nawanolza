package ssafy.nawanolza.server.domain.entity.dto.member;

import lombok.Data;

@Data
public class KakaoProfile {
    public KakaoAccount kakao_account;

    @Data
    public class KakaoAccount {
        public Profile profile;
        public String email;

        @Data
        public class Profile {
            public String nickname;
            public String profile_image_url;
        }
    }
}