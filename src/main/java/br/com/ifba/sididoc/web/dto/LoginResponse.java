package br.com.ifba.sididoc.web.dto;

//Usando record para melhoria do código
public record LoginResponse(String token, String role) {
}
