package br.com.ifba.sididoc.enums;

public enum ProcessingStatus {

    PENDING(1),
    PROCESSING(2),
    COMPLETED(3),
    ERROR(4);

    private final int code;

    ProcessingStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ProcessingStatus fromCode(int code) {
        for (ProcessingStatus status : ProcessingStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Código de status de processamento inválido: " + code);
    }

}
