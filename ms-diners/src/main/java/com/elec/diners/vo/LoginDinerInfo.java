package com.elec.diners.vo;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginDinerInfo {
    private String nickname;
    private String token;
    private String avatarUrl;
}
