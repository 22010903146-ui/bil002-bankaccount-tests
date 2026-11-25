package tr.edu.sakarya.bil002.whitebox;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.mockito.ArgumentCaptor;
import tr.edu.sakarya.bil002.model.Account;
import tr.edu.sakarya.bil002.model.Transaction;
import tr.edu.sakarya.bil002.model.TransactionType;
import tr.edu.sakarya.bil002.model.TransactionStatus;
import tr.edu.sakarya.bil002.repository.TransactionRepository;
import tr.edu.sakarya.bil002.service.TransactionService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class TransactionServiceWhiteBoxTest {
    private TransactionRepository repository;
    private TransactionService service;
    @BeforeEach void setUp() { repository=mock(TransactionRepository.class); service=new TransactionService(repository); }
    @Test void recordUsesBalanceAndSuccessStatus() {
        Account a=new Account("1234567890","Test","STANDARD",125.0);
        when(repository.save(any(Transaction.class))).thenAnswer(call -> call.getArgument(0));
        Transaction result=service.record(a,TransactionType.DEPOSIT,25.0,"Test deposit");
        ArgumentCaptor<Transaction> captured=ArgumentCaptor.forClass(Transaction.class);
        verify(repository).save(captured.capture());
        assertSame(result,captured.getValue());
        assertEquals(a.getAccountNumber(),result.getAccountNumber());
        assertEquals(TransactionType.DEPOSIT,result.getType());
        assertEquals(TransactionStatus.SUCCESS,result.getStatus());
        assertEquals(25.0,result.getAmount(),0.001); assertEquals(125.0,result.getBalanceAfter(),0.001);
    }
    @Test void historyUsesRepositoryDescendingOrderContract() {
        Transaction t=new Transaction("1234567890",TransactionType.DEPOSIT,1.0,1.0,"d",TransactionStatus.SUCCESS);
        List<Transaction> history=List.of(t);
        when(repository.findByAccountNumberOrderByTimestampDescIdDesc("1234567890")).thenReturn(history);
        assertSame(history,service.getHistory("1234567890"));
        verify(repository).findByAccountNumberOrderByTimestampDescIdDesc("1234567890");
    }
    @Test void emptyDailyHistoryIsZero() {
        when(repository.findByAccountNumberAndTypeAndStatus("1234567890",TransactionType.WITHDRAWAL,
                TransactionStatus.SUCCESS)).thenReturn(List.of());
        assertEquals(0.0,service.getDailyWithdrawalTotal("1234567890"),0.001);
    }
    @Test void sameDaySuccessfulWithdrawalsAreSummed() {
        Transaction a=withdrawal(10.0,LocalDateTime.now());
        Transaction b=withdrawal(25.0,LocalDateTime.now());
        when(repository.findByAccountNumberAndTypeAndStatus("1234567890",TransactionType.WITHDRAWAL,
                TransactionStatus.SUCCESS)).thenReturn(List.of(a,b));
        assertEquals(35.0,service.getDailyWithdrawalTotal("1234567890"),0.001);
    }
    @Tag("hata")
    @Test void previousDayWithdrawalsMustNotConsumeTodaysLimit() {
        Transaction old=withdrawal(100.0,LocalDateTime.now().minusDays(1));
        Transaction today=withdrawal(25.0,LocalDateTime.now());
        when(repository.findByAccountNumberAndTypeAndStatus("1234567890",TransactionType.WITHDRAWAL,
                TransactionStatus.SUCCESS)).thenReturn(List.of(old,today));
        // H-13, requirements §3/G-6. Real code returns 125; execution is pending.
        assertEquals(25.0,service.getDailyWithdrawalTotal("1234567890"),0.001);
    }
    private Transaction withdrawal(double amount,LocalDateTime timestamp) {
        Transaction t=new Transaction("1234567890",TransactionType.WITHDRAWAL,amount,0.0,"w",TransactionStatus.SUCCESS);
        t.setTimestamp(timestamp); return t;
    }
}
