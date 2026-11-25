package tr.edu.sakarya.bil002.blackbox;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import tr.edu.sakarya.bil002.support.BankApi;
@Tag("live")
@EnabledIfEnvironmentVariable(named = "BANK_BASE_URL", matches = "https://lab\\.mykucukkara\\.com/s/.+")
public class LiveAcceptanceTest extends BlackBoxContract {
    public LiveAcceptanceTest() { super(new BankApi()); }
}
