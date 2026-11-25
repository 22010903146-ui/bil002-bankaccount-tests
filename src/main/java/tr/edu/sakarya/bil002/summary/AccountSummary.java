package tr.edu.sakarya.bil002.summary;

import java.math.BigDecimal;

public record AccountSummary(BigDecimal totalDeposited, BigDecimal totalWithdrawn, long transactionCount) {
}
