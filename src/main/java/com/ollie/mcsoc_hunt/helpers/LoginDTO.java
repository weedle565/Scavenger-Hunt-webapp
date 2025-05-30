package com.ollie.mcsoc_hunt.helpers;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LoginDTO {

    private String username;
    private String password;

}
