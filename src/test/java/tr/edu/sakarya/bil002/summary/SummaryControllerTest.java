package tr.edu.sakarya.bil002.summary;
import java.util.List;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
class SummaryControllerTest {
    @Test void exposesSummaryJson() throws Exception {
        SummaryHistoryReader reader=n -> List.of(new SummaryEntry("DEPOSIT","SUCCESS",new BigDecimal("25.50")));
        var mvc=MockMvcBuilders.standaloneSetup(new SummaryController(reader,new SummaryCalculator())).build();
        var result=mvc.perform(get("/api/accounts/1234567890/summary")).andReturn().getResponse();
        assertEquals(200,result.getStatus());
        var body=new ObjectMapper().readTree(result.getContentAsString());
        assertEquals(25.5,body.path("totalDeposited").asDouble(),0.001);
        assertEquals(0.0,body.path("totalWithdrawn").asDouble(),0.001);
        assertEquals(1,body.path("transactionCount").asInt());
    }
    @Test void missingAccountIs404() throws Exception {
        SummaryHistoryReader reader=n -> { throw new ResponseStatusException(HttpStatus.NOT_FOUND); };
        var mvc=MockMvcBuilders.standaloneSetup(new SummaryController(reader,new SummaryCalculator())).build();
        assertEquals(404,mvc.perform(get("/api/accounts/0000000000/summary")).andReturn().getResponse().getStatus());
    }
}
