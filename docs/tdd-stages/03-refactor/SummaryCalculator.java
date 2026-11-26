package tr.edu.sakarya.bil002.summary;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class SummaryCalculator {

    public AccountSummary calculate(List<SummaryEntry> history) {
        List<SummaryEntry> snapshot = List.copyOf(history);
        return new AccountSummary(total(snapshot, "DEPOSIT"), total(snapshot, "WITHDRAWAL"), snapshot.size());
    }

    private BigDecimal total(List<SummaryEntry> history, String type) {
        return history.stream()
                .filter(entry -> entry.status().equals("SUCCESS") && entry.type().equals(type))
                .map(SummaryEntry::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
