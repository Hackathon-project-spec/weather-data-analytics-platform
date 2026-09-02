package in.gov.moes.sih26069.common.enums;

public enum AlertSeverity {
    EXTREME,
    HIGH,
    SEVERE,
    MODERATE,
    MEDIUM,
    MINOR,
    LOW;

    public static AlertSeverity normalize(String val) {
        if (val == null) return MODERATE;
        return switch (val.toUpperCase().trim()) {
            case "EXTREME" -> EXTREME;
            case "HIGH", "SEVERE" -> HIGH;
            case "MEDIUM", "MODERATE" -> MEDIUM;
            case "LOW", "MINOR" -> LOW;
            default -> MODERATE;
        };
    }
}
