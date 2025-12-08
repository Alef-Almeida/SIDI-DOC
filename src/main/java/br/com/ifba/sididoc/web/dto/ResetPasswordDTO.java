package br.com.ifba.sididoc.web.dto;

public record ResetPasswordDTO(String token, String newPassword) {
}
