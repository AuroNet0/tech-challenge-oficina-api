package com.oficina.api.validation;

public final class PlacaValidator {

    private static final String REGEX_PLACA_ANTIGA = "^[A-Z]{3}[0-9]{4}$";
    private static final String REGEX_PLACA_MERCOSUL = "^[A-Z]{3}[0-9][A-Z][0-9]{2}$";

    private PlacaValidator() {
    }

    public static boolean isValid(String placa) {
        if (placa == null || placa.isBlank()) {
            return false;
        }

        String normalized = placa
                .trim()
                .toUpperCase()
                .replace("-", "")
                .replace(" ", "");

        return normalized.matches(REGEX_PLACA_ANTIGA) || normalized.matches(REGEX_PLACA_MERCOSUL);
    }
}
