package tr.edu.sakarya.bil002.integration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import tr.edu.sakarya.bil002.support.MockMvcBankApi;
@SpringBootTest
@AutoConfigureMockMvc
public class AccountControllerIntegrationTest extends EndpointContract {
    @Autowired
    public AccountControllerIntegrationTest(MockMvc mvc) { super(MockMvcBankApi.create(mvc)); }
}
