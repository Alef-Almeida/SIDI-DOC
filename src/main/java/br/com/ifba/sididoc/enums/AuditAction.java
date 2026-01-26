package br.com.ifba.sididoc.enums;

public enum AuditAction {

    LOGIN("Login no sistema"),
    LOGIN_FAILED("Falha no login"),

    USER_REGISTRATION("Cadastro de usuário"),
    COMPLETE_REGISTRATION("Conclusão de cadastro"),

    UPDATE_MY_PROFILE("Atualização do próprio perfil"),
    UPDATE_USER_PROFILE("Atualização de perfil de usuário"),

    SEND_ACTIVATION_EMAIL("Envio de e-mail de ativação"),
    SEND_PASSWORD_RESET_EMAIL("Envio de e-mail de redefinição de senha"),
    RESET_PASSWORD("Redefinição de senha"),

    SECTOR_SWITCH_ATTEMPT("Tentativa de troca de setor"),
    SECTOR_SWITCH_SUCCESS("Troca de setor realizada"),
    SECTOR_ACCESS_DENIED("Acesso negado ao setor"),
    SET_CURRENT_SECTOR("Definição do setor atual"),

    ADD_USER_TO_SECTOR("Vinculação de usuário ao setor"),
    REMOVE_USER_FROM_SECTOR("Remoção de usuário do setor"),

    DELETE_OWN_USER_ATTEMPT("Tentativa de exclusão do próprio usuário"),
    DELETE_USER_ADMIN("Tentativa de exclusão de usuário com permissão de administrador"),
    DELETE_USER_PERMISSION_DENIED("Tentativa de exclusão sem permissão"),
    DELETE_USER("Exclusão de usuário");

    private final String label;

    AuditAction(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
