package tr.edu.sakarya.bil002.summary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tr.edu.sakarya.bil002.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SummaryOriginalServiceIntegrationTest {
    @Autowired private AccountService accounts;
    @Autowired private MockMvc mvc;
    @Test void summaryUsesOriginalAccountHistoryAndPreservesBalance() throws Exception {
        String n=accounts.createAccount("Summary","STANDARD",100.0).getAccountNumber();
        accounts.deposit(n,25.0);accounts.withdraw(n,5.0);accounts.freezeAccount(n);
        var response=mvc.perform(get("/api/accounts/"+n+"/summary")).andReturn().getResponse();
        assertEquals(200,response.getStatus());
        var json=new ObjectMapper().readTree(response.getContentAsString());
        assertEquals(25.0,json.path("totalDeposited").asDouble(),0.001);
        assertEquals(5.0,json.path("totalWithdrawn").asDouble(),0.001);
        assertEquals(2,json.path("transactionCount").asInt());
        assertEquals(120.0,accounts.getAccount(n).getBalance(),0.001);
    }
    @Test void missingAccountSummaryUsesOriginalNotFoundHandler() throws Exception {
        assertEquals(404,mvc.perform(get("/api/accounts/0000000000/summary")).andReturn().getResponse().getStatus());
    }
}
