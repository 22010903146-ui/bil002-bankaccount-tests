package tr.edu.sakarya.bil002.summary;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class SummaryCalculator {

    public AccountSummary calculate(List<SummaryEntry> history) {
        BigDecimal deposited = BigDecimal.ZERO;
        BigDecimal withdrawn = BigDecimal.ZERO;
        for (SummaryEntry entry : history) {
            if (entry.status().equals("SUCCESS") && entry.type().equals("DEPOSIT")) {
                deposited = deposited.add(entry.amount());
            }
            if (entry.status().equals("SUCCESS") && entry.type().equals("WITHDRAWAL")) {
                withdrawn = withdrawn.add(entry.amount());
            }
        }
        return new AccountSummary(deposited, withdrawn, history.size());
    }
}
