package tr.edu.sakarya.bil002.bdd;
import tr.edu.sakarya.bil002.support.BankApi;
import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import static org.junit.jupiter.api.Assertions.*;

public class BankSteps {
    private final BankApi api;
    public BankSteps(org.springframework.test.web.servlet.MockMvc mvc) {
        api = tr.edu.sakarya.bil002.support.MockMvcBankApi.create(mvc);
    }
    private String account;
    private String target;
    private BankApi.Reply reply;
    private JsonNode before;
    @Given("a {string} account with balance {double}")
    public void account(String type,double balance) { account=api.create(type,balance); }
    @Given("the account is {string}")
    public void state(String state) { api.state(account,state); }
    @Given("an active target account with balance {double}")
    public void target(double balance) { target=api.create("STANDARD",balance); }
    @When("the owner deposits {double} TL")
    public void deposit(double amount) { before=api.account(account); reply=api.operation(account,"deposit",amount); }
    @When("the owner withdraws {double} TL")
    public void withdraw(double amount) { before=api.account(account); reply=api.operation(account,"withdraw",amount); }
    @When("the owner transfers {double} TL to the target")
    public void transfer(double amount) { before=api.account(account); reply=api.transfer(account,target,amount); }
    @When("the owner requests {string}")
    public void operation(String operation) { before=api.account(account); reply=api.operation(account,operation,null); }
    @Then("the response code is {int}")
    public void code(int code) { BankApi.code(reply,code); }
    @Then("the account balance is {double} TL")
    public void balance(double balance) { assertEquals(balance,api.balance(account),0.001); }
    @Then("the target balance is {double} TL")
    public void targetBalance(double balance) { assertEquals(balance,api.balance(target),0.001); }
    @Then("the account state is {string}")
    public void accountState(String state) { assertEquals(state,api.account(account).path("status").asText()); }
    @Then("the account is unchanged")
    public void unchanged() { api.unchanged(account,before); }
    @Then("the transaction history has {int} entries")
    public void history(int count) { assertEquals(count,api.history(account).size()); }
    @Then("the latest transaction is {string} with status {string}")
    public void latest(String type,String status) {
        JsonNode h=api.history(account); assertFalse(h.isEmpty());
        assertEquals(type,h.get(0).path("type").asText());
        assertEquals(status,h.get(0).path("status").asText());
    }
}
