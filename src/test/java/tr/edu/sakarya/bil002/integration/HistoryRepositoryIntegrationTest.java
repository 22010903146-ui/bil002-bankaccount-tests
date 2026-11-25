package tr.edu.sakarya.bil002.integration;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import tr.edu.sakarya.bil002.model.Transaction;
import tr.edu.sakarya.bil002.model.TransactionType;
import tr.edu.sakarya.bil002.model.TransactionStatus;
import tr.edu.sakarya.bil002.repository.TransactionRepository;
import tr.edu.sakarya.bil002.service.TransactionService;
import tr.edu.sakarya.bil002.service.AccountService;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Transactional
class HistoryRepositoryIntegrationTest {
    @Autowired private TransactionRepository repository;
    @Autowired private TransactionService transactions;
    @Autowired private AccountService accounts;
    private Transaction item(String number,TransactionType type,TransactionStatus status,double amount,int day) {
        Transaction result=new Transaction(number,type,amount,100.0,"fixture",status);
        result.setTimestamp(LocalDate.now().plusDays(day).atTime(12,0));return result;
    }
    @Test void dailyTotalExcludesFailedAndOtherTypesAndOtherAccounts() {
        String a=accounts.createAccount("Daily A","STANDARD",1000.0).getAccountNumber();
        String b=accounts.createAccount("Daily B","STANDARD",1000.0).getAccountNumber();
        repository.saveAllAndFlush(List.of(item(a,TransactionType.WITHDRAWAL,TransactionStatus.SUCCESS,25,0),
                item(a,TransactionType.WITHDRAWAL,TransactionStatus.FAILED,100,0),
                item(a,TransactionType.TRANSFER,TransactionStatus.SUCCESS,50,0),
                item(b,TransactionType.WITHDRAWAL,TransactionStatus.SUCCESS,30,0)));
        assertEquals(25.0,transactions.getDailyWithdrawalTotal(a),0.001);
    }
    @Tag("hata")
    @Test void dailyTotalExcludesPreviousCalendarDay() {
        String a=accounts.createAccount("Daily date","STANDARD",1000.0).getAccountNumber();
        repository.saveAllAndFlush(List.of(item(a,TransactionType.WITHDRAWAL,TransactionStatus.SUCCESS,25,0),
                item(a,TransactionType.WITHDRAWAL,TransactionStatus.SUCCESS,100,-1)));
        assertEquals(25.0,transactions.getDailyWithdrawalTotal(a),0.001);
    }
    @Tag("hata")
    @Test void rejectedWithdrawalMustHaveFailedHistoryRecord() {
        String a=accounts.createAccount("Failed history","STANDARD",100.0).getAccountNumber();
        assertThrows(IllegalArgumentException.class,()->accounts.withdraw(a,-0.01));
        List<Transaction> history=accounts.getTransactionHistory(a);
        assertEquals(1,history.size());assertEquals(TransactionStatus.FAILED,history.get(0).getStatus());
        assertEquals(100.0,history.get(0).getBalanceAfter(),0.001);
    }
}
