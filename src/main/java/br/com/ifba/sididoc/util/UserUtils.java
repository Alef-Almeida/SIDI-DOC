package br.com.ifba.sididoc.util;

import br.com.ifba.sididoc.jwt.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserUtils {

    private UserUtils() {}

    public static CustomUserDetails getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) authentication.getPrincipal();
        }

        throw new RuntimeException("Nenhum usuário autenticado encontrado no contexto atual.");
    }

    public static Long getAuthenticatedUserId() {
        return getAuthenticatedUser().getId();
    }

    public static String getAuthenticatedUserEmail() {
        return getAuthenticatedUser().getUsername();
    }

    public static Long getCurrentSectorId() {
        return getAuthenticatedUser().getCurrentSectorId();
    }
}