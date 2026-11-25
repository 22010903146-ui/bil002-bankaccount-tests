package tr.edu.sakarya.bil002.blackbox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import tr.edu.sakarya.bil002.support.MockMvcBankApi;
@SpringBootTest
@AutoConfigureMockMvc
public class AccountServiceBlackBoxTest extends BlackBoxContract {
    @Autowired
    public AccountServiceBlackBoxTest(MockMvc mvc) { super(MockMvcBankApi.create(mvc)); }
}
