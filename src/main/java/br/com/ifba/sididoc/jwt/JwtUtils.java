package br.com.ifba.sididoc.jwt;

import br.com.ifba.sididoc.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtils {

    private final Key key;
    private final long expirationMillis;

    //Variaveis de configuração do JWT
    public JwtUtils(
            @Value("${SECURITY_JWT_SECRET}") String secret,
            @Value("${JWT_EXPIRATION:3600000}") long expirationMillis
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMillis;
    }

    //Geração do token JWT de login
    public String generateToken(UserDetails userDetails) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMillis)))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equalsIgnoreCase(userDetails.getUsername()) && !isTokenExpired(token);
    }

    //Token de ativação de conta
    public String generateActivationToken(User user) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(1, ChronoUnit.DAYS)))
                .claim("type", "activation")
                .signWith(key)
                .compact();
    }

    public String extractEmailFromActivationToken(String token) {
        Claims claims = extractAllClaims(token);

        if (!"activation".equals(claims.get("type")))
            throw new RuntimeException("Token inválido para ativação.");

        return claims.getSubject();
    }

    //Gerando token para redefinição de senha
    public String generateResetPasswordToken(User user) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(1, ChronoUnit.DAYS)))
                .claim("type", "reset")
                .signWith(key)
                .compact();
    }

    //Fazendo a extração do email do token de redefinição de senha
    public String extractEmailFromResetToken(String token) {
        Claims claims = extractAllClaims(token);

        if (!"reset".equals(claims.get("type")))
            throw new RuntimeException("Token inválido para redefinição de senha.");

        return claims.getSubject();
    }

    //Verificando se o token expirou
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
