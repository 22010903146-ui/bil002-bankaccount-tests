package tr.edu.sakarya.bil002.whitebox;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.DisplayName;
import tr.edu.sakarya.bil002.model.Account;
import tr.edu.sakarya.bil002.model.AccountStatus;
import tr.edu.sakarya.bil002.model.TransactionType;
import tr.edu.sakarya.bil002.repository.AccountRepository;
import tr.edu.sakarya.bil002.service.AccountService;
import tr.edu.sakarya.bil002.service.TransactionService;
import tr.edu.sakarya.bil002.exception.AccountNotFoundException;
import tr.edu.sakarya.bil002.exception.InsufficientBalanceException;
import tr.edu.sakarya.bil002.exception.InvalidAccountStateException;
import tr.edu.sakarya.bil002.exception.LimitExceededException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class AccountServiceWhiteBoxTest {
    private static final String N = "1234567890";
    private static final String M = "2234567890";
    private AccountRepository repository;
    private TransactionService transactions;
    private AccountService service;
    @BeforeEach void setUp() {
        repository=mock(AccountRepository.class);
        transactions=mock(TransactionService.class);
        service=new AccountService(repository,transactions);
        when(repository.save(any(Account.class))).thenAnswer(call -> call.getArgument(0));
    }
    private Account account(String number,String type,double balance) {
        Account account=new Account(number,"Test",type,balance);
        when(repository.findByAccountNumber(number)).thenReturn(Optional.of(account));
        return account;
    }
    @Test
    @DisplayName("WB-01: nullOwnerRejected")
    void nullOwnerRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.createAccount(null,"STANDARD",0.0));
    }
    @Test
    @DisplayName("WB-02: blankOwnerRejected")
    void blankOwnerRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.createAccount("  ","STANDARD",0.0));
    }
    @Test
    @DisplayName("WB-03: nullTypeRejected")
    void nullTypeRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.createAccount("Ali",null,0.0));
    }
    @Test
    @DisplayName("WB-04: unknownTypeRejected")
    void unknownTypeRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.createAccount("Ali","OTHER",0.0));
    }
    @Test
    @DisplayName("WB-05: standardNullBalanceAndTrim")
    void standardNullBalanceAndTrim() {
        Account a=service.createAccount(" Ali ","STANDARD",null); assertEquals("Ali",a.getOwnerName()); assertEquals(0.0,a.getBalance(),0.001); assertEquals(AccountStatus.ACTIVE,a.getStatus()); assertTrue(a.getAccountNumber().matches("[1-9][0-9]{9}")); verifyNoInteractions(transactions);
    }
    @Test
    @DisplayName("WB-06: premiumPositiveBalance")
    void premiumPositiveBalance() {
        Account a=service.createAccount("Ali","PREMIUM",10.25); assertEquals("PREMIUM",a.getAccountType()); assertEquals(10.25,a.getBalance(),0.001);
    }
    @Test
    @Tag("hata") // H-01; verified by the local test run
    @DisplayName("WB-07: negativeInitialBalanceRejected")
    void negativeInitialBalanceRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.createAccount("Ali","STANDARD",-0.01));
    }
    @Test
    @DisplayName("WB-08: findPresentAndAbsent")
    void findPresentAndAbsent() {
        Account a=account(N,"STANDARD",100); assertSame(a,service.findAccount(N).orElseThrow()); assertTrue(service.findAccount("0000000000").isEmpty());
    }
    @Test
    @DisplayName("WB-09: getMissingAccount")
    void getMissingAccount() {
        assertThrows(AccountNotFoundException.class, () -> service.getAccount("0000000000"));
    }
    @Test
    @DisplayName("WB-10: getAllAccountsDelegates")
    void getAllAccountsDelegates() {
        Account a=account(N,"STANDARD",100); when(repository.findAll()).thenReturn(List.of(a)); assertEquals(List.of(a),service.getAllAccounts());
    }
    @Test
    @DisplayName("WB-11: nullDeposit")
    void nullDeposit() {
        Account a=account(N,"STANDARD",100); assertThrows(IllegalArgumentException.class,()->service.deposit(N,null)); assertEquals(100.0,a.getBalance(),0.001); verify(repository,never()).save(any(Account.class));
    }
    @Test
    @DisplayName("WB-12: negativeDeposit")
    void negativeDeposit() {
        Account a=account(N,"STANDARD",100); assertThrows(IllegalArgumentException.class,()->service.deposit(N,-0.01)); assertEquals(100.0,a.getBalance(),0.001); verify(repository,never()).save(any(Account.class));
    }
    @Test
    @Tag("hata") // H-02; verified by the local test run
    @DisplayName("WB-13: zeroDepositRejected")
    void zeroDepositRejected() {
        account(N,"STANDARD",100); assertThrows(IllegalArgumentException.class,()->service.deposit(N,0.0));
    }
    @Test
    @DisplayName("WB-14: depositFROZEN")
    void depositFROZEN() {
        Account a=account(N,"STANDARD",0); a.setStatus(AccountStatus.FROZEN); assertThrows(InvalidAccountStateException.class,()->service.deposit(N,1.0)); assertEquals(0.0,a.getBalance(),0.001);
    }
    @Test
    @DisplayName("WB-15: depositCLOSED")
    void depositCLOSED() {
        Account a=account(N,"STANDARD",0); a.setStatus(AccountStatus.CLOSED); assertThrows(InvalidAccountStateException.class,()->service.deposit(N,1.0)); assertEquals(0.0,a.getBalance(),0.001);
    }
    @Test
    @DisplayName("WB-16: depositAtLimitSTANDARD")
    void depositAtLimitSTANDARD() {
        Account a=account(N,"STANDARD",0); assertSame(a,service.deposit(N,10000.0)); assertEquals(10000.0,a.getBalance(),0.001); verify(transactions).record(a,TransactionType.DEPOSIT,10000.0,"Para yatırma");
    }
    @Test
    @DisplayName("WB-17: depositAtLimitPREMIUM")
    void depositAtLimitPREMIUM() {
        Account a=account(N,"PREMIUM",0); assertSame(a,service.deposit(N,50000.0)); assertEquals(50000.0,a.getBalance(),0.001); verify(transactions).record(a,TransactionType.DEPOSIT,50000.0,"Para yatırma");
    }
    @Test
    @DisplayName("WB-18: depositOverLimitSTANDARD")
    void depositOverLimitSTANDARD() {
        Account a=account(N,"STANDARD",0); assertThrows(LimitExceededException.class,()->service.deposit(N,10000.01)); assertEquals(0.0,a.getBalance(),0.001);
    }
    @Test
    @DisplayName("WB-19: depositOverLimitPREMIUM")
    void depositOverLimitPREMIUM() {
        Account a=account(N,"PREMIUM",0); assertThrows(LimitExceededException.class,()->service.deposit(N,50000.01)); assertEquals(0.0,a.getBalance(),0.001);
    }
    @Test
    @DisplayName("WB-20: nullWithdrawal")
    void nullWithdrawal() {
        Account a=account(N,"STANDARD",100); assertThrows(IllegalArgumentException.class,()->service.withdraw(N,null)); assertEquals(100.0,a.getBalance(),0.001);
    }
    @Test
    @DisplayName("WB-21: zeroWithdrawal")
    void zeroWithdrawal() {
        Account a=account(N,"STANDARD",100); assertThrows(IllegalArgumentException.class,()->service.withdraw(N,0.0)); assertEquals(100.0,a.getBalance(),0.001);
    }
    @Test
    @DisplayName("WB-22: negativeWithdrawal")
    void negativeWithdrawal() {
        Account a=account(N,"STANDARD",100); assertThrows(IllegalArgumentException.class,()->service.withdraw(N,-0.01)); assertEquals(100.0,a.getBalance(),0.001);
    }
    @Test
    @DisplayName("WB-23: insufficientWithdrawal")
    void insufficientWithdrawal() {
        Account a=account(N,"STANDARD",100); assertThrows(InsufficientBalanceException.class,()->service.withdraw(N,100.01)); assertEquals(100.0,a.getBalance(),0.001);
    }
    @Test
    @Tag("hata") // H-03; verified by the local test run
    @DisplayName("WB-24: fullBalanceWithdrawalAllowed")
    void fullBalanceWithdrawalAllowed() {
        Account a=account(N,"STANDARD",100); service.withdraw(N,100.0); assertEquals(0.0,a.getBalance(),0.001);
    }
    @Test
    @DisplayName("WB-25: standardDailyExcess")
    void standardDailyExcess() {
        Account a=account(N,"STANDARD",10000); when(transactions.getDailyWithdrawalTotal(N)).thenReturn(4999.99); assertThrows(LimitExceededException.class,()->service.withdraw(N,0.02)); assertEquals(10000.0,a.getBalance(),0.001);
    }
    @Test
    @DisplayName("WB-26: standardDailyEqualityAllowed")
    void standardDailyEqualityAllowed() {
        Account a=account(N,"STANDARD",10000); when(transactions.getDailyWithdrawalTotal(N)).thenReturn(4999.99); service.withdraw(N,0.01); assertEquals(9999.99,a.getBalance(),0.001);
    }
    @Test
    @DisplayName("WB-27: premiumDailyExcess")
    void premiumDailyExcess() {
        Account a=account(N,"PREMIUM",30000); when(transactions.getDailyWithdrawalTotal(N)).thenReturn(19999.99); assertThrows(LimitExceededException.class,()->service.withdraw(N,0.02)); assertEquals(30000.0,a.getBalance(),0.001);
    }
    @Test
    @Tag("hata") // H-04; verified by the local test run
    @DisplayName("WB-28: premiumDailyEqualityAllowed")
    void premiumDailyEqualityAllowed() {
        Account a=account(N,"PREMIUM",30000); when(transactions.getDailyWithdrawalTotal(N)).thenReturn(19999.99); service.withdraw(N,0.01); assertEquals(29999.99,a.getBalance(),0.001);
    }
    @Test
    @DisplayName("WB-29: premiumBelowDailyLimit")
    void premiumBelowDailyLimit() {
        Account a=account(N,"PREMIUM",30000); when(transactions.getDailyWithdrawalTotal(N)).thenReturn(19000.0); service.withdraw(N,500.0); assertEquals(29500.0,a.getBalance(),0.001);
    }
    @Test
    @Tag("hata") // H-10; verified by the local test run
    @DisplayName("WB-30: frozenWithdrawalRejected")
    void frozenWithdrawalRejected() {
        Account a=account(N,"STANDARD",100); a.setStatus(AccountStatus.FROZEN); assertThrows(InvalidAccountStateException.class,()->service.withdraw(N,25.0)); assertEquals(100.0,a.getBalance(),0.001);
    }
    @Test
    @DisplayName("WB-31: nullTransfer")
    void nullTransfer() {
        assertThrows(IllegalArgumentException.class,()->service.transfer(N,M,null)); verifyNoInteractions(repository);
    }
    @Test
    @DisplayName("WB-32: zeroTransfer")
    void zeroTransfer() {
        assertThrows(IllegalArgumentException.class,()->service.transfer(N,M,0.0)); verifyNoInteractions(repository);
    }
    @Test
    @DisplayName("WB-33: negativeTransfer")
    void negativeTransfer() {
        assertThrows(IllegalArgumentException.class,()->service.transfer(N,M,-0.01)); verifyNoInteractions(repository);
    }
    @Test
    @DisplayName("WB-34: missingTransferSource")
    void missingTransferSource() {
        assertThrows(AccountNotFoundException.class,()->service.transfer(N,M,1.0));
    }
    @Test
    @DisplayName("WB-35: missingTransferTarget")
    void missingTransferTarget() {
        Account a=account(N,"STANDARD",100); assertThrows(AccountNotFoundException.class,()->service.transfer(N,M,1.0)); assertEquals(100.0,a.getBalance(),0.001);
    }
    @Test
    @DisplayName("WB-36: transferSourceFROZEN")
    void transferSourceFROZEN() {
        Account a=account(N,"STANDARD",0); a.setStatus(AccountStatus.FROZEN); Account b=account(M,"STANDARD",0); assertThrows(InvalidAccountStateException.class,()->service.transfer(N,M,1.0)); assertEquals(0.0,a.getBalance(),0.001); assertEquals(0.0,b.getBalance(),0.001);
    }
    @Test
    @DisplayName("WB-37: transferSourceCLOSED")
    void transferSourceCLOSED() {
        Account a=account(N,"STANDARD",0); a.setStatus(AccountStatus.CLOSED); Account b=account(M,"STANDARD",0); assertThrows(InvalidAccountStateException.class,()->service.transfer(N,M,1.0)); assertEquals(0.0,a.getBalance(),0.001); assertEquals(0.0,b.getBalance(),0.001);
    }
    @Test
    @Tag("hata") // H-08; verified by the local test run
    @DisplayName("WB-38: transferTargetFROZEN")
    void transferTargetFROZEN() {
        Account a=account(N,"STANDARD",100); Account b=account(M,"STANDARD",0); b.setStatus(AccountStatus.FROZEN); assertThrows(InvalidAccountStateException.class,()->service.transfer(N,M,25.0)); assertEquals(100.0,a.getBalance(),0.001); assertEquals(0.0,b.getBalance(),0.001);
    }
    @Test
    @Tag("hata") // H-08; verified by the local test run
    @DisplayName("WB-39: transferTargetCLOSED")
    void transferTargetCLOSED() {
        Account a=account(N,"STANDARD",100); Account b=account(M,"STANDARD",0); b.setStatus(AccountStatus.CLOSED); assertThrows(InvalidAccountStateException.class,()->service.transfer(N,M,25.0)); assertEquals(100.0,a.getBalance(),0.001); assertEquals(0.0,b.getBalance(),0.001);
    }
    @Test
    @Tag("hata") // H-07; verified by the local test run
    @DisplayName("WB-40: sameAccountTransferRejected")
    void sameAccountTransferRejected() {
        account(N,"STANDARD",100); assertThrows(IllegalArgumentException.class,()->service.transfer(N,N,25.0));
    }
    @Test
    @DisplayName("WB-41: insufficientTransferKeepsBothAccounts")
    void insufficientTransferKeepsBothAccounts() {
        Account a=account(N,"STANDARD",100); Account b=account(M,"STANDARD",0); assertThrows(InsufficientBalanceException.class,()->service.transfer(N,M,100.01)); assertEquals(100.0,a.getBalance(),0.001); assertEquals(0.0,b.getBalance(),0.001); verify(repository,never()).save(any(Account.class)); verifyNoInteractions(transactions);
    }
    @Test
    @DisplayName("WB-42: fullTransferMaintainsTotalAndTwoRecords")
    void fullTransferMaintainsTotalAndTwoRecords() {
        Account a=account(N,"STANDARD",100); Account b=account(M,"PREMIUM",0); service.transfer(N,M,100.0); assertEquals(0.0,a.getBalance(),0.001); assertEquals(100.0,b.getBalance(),0.001); verify(transactions).record(eq(a),eq(TransactionType.TRANSFER),eq(100.0),anyString()); verify(transactions).record(eq(b),eq(TransactionType.TRANSFER),eq(100.0),anyString());
    }
    @Test
    @DisplayName("WB-43: freezeActiveKeepsBalance")
    void freezeActiveKeepsBalance() {
        Account a=account(N,"STANDARD",10); service.freezeAccount(N); assertEquals(AccountStatus.FROZEN,a.getStatus()); assertEquals(10.0,a.getBalance(),0.001);
    }
    @Test
    @DisplayName("WB-44: freezeClosedRejected")
    void freezeClosedRejected() {
        Account a=account(N,"STANDARD",0); a.setStatus(AccountStatus.CLOSED); assertThrows(InvalidAccountStateException.class,()->service.freezeAccount(N));
    }
    @Test
    @Tag("hata") // H-06; verified by the local test run
    @DisplayName("WB-45: freezeAlreadyFrozenRejected")
    void freezeAlreadyFrozenRejected() {
        Account a=account(N,"STANDARD",10); a.setStatus(AccountStatus.FROZEN); assertThrows(InvalidAccountStateException.class,()->service.freezeAccount(N));
    }
    @Test
    @DisplayName("WB-46: unfreezeFrozenKeepsBalance")
    void unfreezeFrozenKeepsBalance() {
        Account a=account(N,"STANDARD",10); a.setStatus(AccountStatus.FROZEN); service.unfreezeAccount(N); assertEquals(AccountStatus.ACTIVE,a.getStatus()); assertEquals(10.0,a.getBalance(),0.001);
    }
    @Test
    @DisplayName("WB-47: unfreezeACTIVERejected")
    void unfreezeACTIVERejected() {
        Account a=account(N,"STANDARD",0); a.setStatus(AccountStatus.ACTIVE); assertThrows(InvalidAccountStateException.class,()->service.unfreezeAccount(N)); assertEquals(AccountStatus.ACTIVE,a.getStatus());
    }
    @Test
    @DisplayName("WB-48: unfreezeCLOSEDRejected")
    void unfreezeCLOSEDRejected() {
        Account a=account(N,"STANDARD",0); a.setStatus(AccountStatus.CLOSED); assertThrows(InvalidAccountStateException.class,()->service.unfreezeAccount(N)); assertEquals(AccountStatus.CLOSED,a.getStatus());
    }
    @Test
    @DisplayName("WB-49: closeZeroBalanceACTIVE")
    void closeZeroBalanceACTIVE() {
        Account a=account(N,"STANDARD",0); a.setStatus(AccountStatus.ACTIVE); service.closeAccount(N); assertEquals(AccountStatus.CLOSED,a.getStatus());
    }
    @Test
    @DisplayName("WB-50: closeZeroBalanceFROZEN")
    void closeZeroBalanceFROZEN() {
        Account a=account(N,"STANDARD",0); a.setStatus(AccountStatus.FROZEN); service.closeAccount(N); assertEquals(AccountStatus.CLOSED,a.getStatus());
    }
    @Test
    @DisplayName("WB-51: closeAlreadyClosedRejected")
    void closeAlreadyClosedRejected() {
        Account a=account(N,"STANDARD",0); a.setStatus(AccountStatus.CLOSED); assertThrows(InvalidAccountStateException.class,()->service.closeAccount(N));
    }
    @Test
    @Tag("hata") // H-05; verified by the local test run
    @DisplayName("WB-52: closePositiveBalanceRejected")
    void closePositiveBalanceRejected() {
        Account a=account(N,"STANDARD",10); assertThrows(InvalidAccountStateException.class,()->service.closeAccount(N)); assertEquals(AccountStatus.ACTIVE,a.getStatus()); assertEquals(10.0,a.getBalance(),0.001);
    }
    @Test
    @Tag("hata") // H-09; verified by the local test run
    @DisplayName("WB-53: monthlyInterestWB53")
    void monthlyInterestWB53() {
        Account a=account(N,"STANDARD",1000); service.calculateInterest(N); assertEquals(1002.5,a.getBalance(),0.001);
    }
    @Test
    @Tag("hata") // H-09; verified by the local test run
    @DisplayName("WB-54: monthlyInterestWB54")
    void monthlyInterestWB54() {
        Account a=account(N,"PREMIUM",1000); service.calculateInterest(N); assertEquals(1004.17,a.getBalance(),0.001);
    }
    @Test
    @Tag("hata") // H-09; verified by the local test run
    @DisplayName("WB-55: monthlyInterestWB55")
    void monthlyInterestWB55() {
        Account a=account(N,"PREMIUM",2500.5); service.calculateInterest(N); assertEquals(2510.92,a.getBalance(),0.001);
    }
    @Test
    @DisplayName("WB-56: zeroInterestStillRecorded")
    void zeroInterestStillRecorded() {
        Account a=account(N,"STANDARD",0); service.calculateInterest(N); assertEquals(0.0,a.getBalance(),0.001); verify(transactions).record(eq(a),eq(TransactionType.INTEREST),eq(0.0),anyString());
    }
    @Test
    @DisplayName("WB-57: interestFROZENRejected")
    void interestFROZENRejected() {
        Account a=account(N,"STANDARD",0); a.setStatus(AccountStatus.FROZEN); assertThrows(InvalidAccountStateException.class,()->service.calculateInterest(N));
    }
    @Test
    @DisplayName("WB-58: interestCLOSEDRejected")
    void interestCLOSEDRejected() {
        Account a=account(N,"STANDARD",0); a.setStatus(AccountStatus.CLOSED); assertThrows(InvalidAccountStateException.class,()->service.calculateInterest(N));
    }
    @Test
    @DisplayName("WB-59: historyRequiresExistingAccount")
    void historyRequiresExistingAccount() {
        assertThrows(AccountNotFoundException.class,()->service.getTransactionHistory(N)); verifyNoInteractions(transactions);
    }
    @Test
    @DisplayName("WB-60: historyAvailableForFrozenAccount")
    void historyAvailableForFrozenAccount() {
        Account a=account(N,"STANDARD",0); a.setStatus(AccountStatus.FROZEN); when(transactions.getHistory(N)).thenReturn(List.of()); assertTrue(service.getTransactionHistory(N).isEmpty());
    }
}
