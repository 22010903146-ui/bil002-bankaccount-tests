package tr.edu.sakarya.bil002.summary;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SummaryController {

    private final SummaryHistoryReader history;
    private final SummaryCalculator calculator;

    public SummaryController(SummaryHistoryReader history, SummaryCalculator calculator) {
        this.history = history;
        this.calculator = calculator;
    }

    @GetMapping(value = "/api/accounts/{accountNumber}/summary", produces = "application/json")
    public AccountSummary summary(@PathVariable("accountNumber") String accountNumber) {
        return calculator.calculate(history.history(accountNumber));
    }
}
