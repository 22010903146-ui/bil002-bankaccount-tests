package tr.edu.sakarya.bil002.summary;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SummaryCalculatorTest {
    private final SummaryCalculator calculator=new SummaryCalculator();
    private SummaryEntry entry(String type,String status,String amount) {
        return new SummaryEntry(type,status,new BigDecimal(amount));
    }
    @Test void emptyHistory() {
        AccountSummary s=calculator.calculate(List.of());
        assertEquals(0,s.totalDeposited().compareTo(BigDecimal.ZERO));
        assertEquals(0,s.totalWithdrawn().compareTo(BigDecimal.ZERO)); assertEquals(0,s.transactionCount());
    }
    @Test void sumsOnlySuccessfulDepositsAndWithdrawals() {
        AccountSummary s=calculator.calculate(List.of(entry("DEPOSIT","SUCCESS","10.25"),
                entry("DEPOSIT","SUCCESS","2.50"),entry("WITHDRAWAL","SUCCESS","3.00")));
        assertEquals(0,s.totalDeposited().compareTo(new BigDecimal("12.75")));
        assertEquals(0,s.totalWithdrawn().compareTo(new BigDecimal("3.00")));assertEquals(3,s.transactionCount());
    }
    @Test void failedAttemptsDoNotChangeTotalsButAreCounted() {
        AccountSummary s=calculator.calculate(List.of(entry("DEPOSIT","FAILED","-1"),entry("WITHDRAWAL","FAILED","100")));
        assertEquals(0,s.totalDeposited().compareTo(BigDecimal.ZERO));
        assertEquals(0,s.totalWithdrawn().compareTo(BigDecimal.ZERO));assertEquals(2,s.transactionCount());
    }
    @Test void interestAndTransfersAreNotCashDepositsOrWithdrawals() {
        AccountSummary s=calculator.calculate(List.of(entry("TRANSFER","SUCCESS","100"),entry("INTEREST","SUCCESS","1.25")));
        assertEquals(0,s.totalDeposited().compareTo(BigDecimal.ZERO));
        assertEquals(0,s.totalWithdrawn().compareTo(BigDecimal.ZERO));assertEquals(2,s.transactionCount());
    }
    @Test void exactDecimalArithmetic() {
        AccountSummary s=calculator.calculate(List.of(entry("DEPOSIT","SUCCESS","0.10"),entry("DEPOSIT","SUCCESS","0.20")));
        assertEquals(0,s.totalDeposited().compareTo(new BigDecimal("0.30")));
    }
    @Test void doesNotMutateHistory() {
        List<SummaryEntry> history=new ArrayList<>(List.of(entry("DEPOSIT","SUCCESS","1")));
        List<SummaryEntry> before=List.copyOf(history);calculator.calculate(history);assertEquals(before,history);
    }
}
