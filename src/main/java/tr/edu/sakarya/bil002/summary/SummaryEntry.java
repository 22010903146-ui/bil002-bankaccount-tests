package tr.edu.sakarya.bil002.summary;

import java.math.BigDecimal;

public record SummaryEntry(String type, String status, BigDecimal amount) {
}
