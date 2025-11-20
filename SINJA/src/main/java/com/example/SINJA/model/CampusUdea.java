package com.example.SINJA.model;

public enum CampusUdea {
    ANDES(1),
    APARTADO(2),
    CAREPA(3),
    CARMEN_DE_VIBORAL(4),
    CAUCASIA(5),
    ENVIGADO(6),
    MEDELLIN(7),
    PUERTO_BERRIO(8),
    SANTA_FE_ANTIOQUIA(9),
    TURBO(10);

    private final int code;

    CampusUdea(int code){
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}


