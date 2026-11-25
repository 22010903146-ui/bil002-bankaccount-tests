package tr.edu.sakarya.bil002.summary;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

import tr.edu.sakarya.bil002.service.AccountService;

@Component
public class OriginalSummaryHistoryReader implements SummaryHistoryReader {

    private final AccountService accountService;

    public OriginalSummaryHistoryReader(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public List<SummaryEntry> history(String accountNumber) {
        return accountService.getTransactionHistory(accountNumber).stream()
                .map(transaction -> new SummaryEntry(transaction.getType().name(), transaction.getStatus().name(),
                        BigDecimal.valueOf(transaction.getAmount())))
                .toList();
    }
}
