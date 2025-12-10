package br.com.ifba.sididoc.web.dto;

//Usando record para melhoria do código
public record ResetPasswordDTO(String token, String newPassword) {
}
