package com.auth.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
  @NotBlank(message = "이메일을 입력해주세요.")
  @Email(message = "올바른 이메일 형식이 아닙니다.")
  String email,

  @NotBlank(message = "비밀번호를 입력해주세요.")
  @Size(min = 8, max = 16, message = "비밀번호는 8 ~ 16자 이내여야 합니다.")
  String password,

  @NotBlank(message = "유저명을 입력해주세요.")
  @Size(min = 4, max = 16, message = "유저명은 4 ~ 16자 이내여야 합니다.")
  String username
) {
}
