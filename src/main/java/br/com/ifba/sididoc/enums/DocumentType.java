package br.com.ifba.sididoc.enums;

public enum DocumentType {

    IMAGE(1),
    PDF(2);

    private final int code;

    DocumentType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static DocumentType fromCode(int code) {
        for (DocumentType type : DocumentType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Código de tipo de documento inválido: " + code);
    }
}
