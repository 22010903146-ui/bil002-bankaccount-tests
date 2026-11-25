package tr.edu.sakarya.bil002.integration;
import tr.edu.sakarya.bil002.support.BankApi;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
public abstract class EndpointContract {
    private final BankApi api;
    protected EndpointContract(BankApi api) { this.api = api; }
    @Test
    @Tag("hata") // H-12: verified by the local test run
    @DisplayName("API-01 | §7 / R-10.2 | Olmayan hesap: get")
    void api_01() {
        BankApi.error(api.send("GET","/api/accounts/0000000000",null),404);
    }

    @Test
    @DisplayName("API-02 | §7 / R-10.2 | Olmayan hesap: deposit")
    void api_02() {
        BankApi.error(api.send("POST","/api/accounts/0000000000/deposit","{\"amount\":1.0}"),404);
    }

    @Test
    @DisplayName("API-03 | §7 / R-10.2 | Olmayan hesap: withdraw")
    void api_03() {
        BankApi.error(api.send("POST","/api/accounts/0000000000/withdraw","{\"amount\":1.0}"),404);
    }

    @Test
    @DisplayName("API-04 | §7 / R-10.2 | Olmayan hesap: freeze")
    void api_04() {
        BankApi.error(api.send("POST","/api/accounts/0000000000/freeze",null),404);
    }

    @Test
    @DisplayName("API-05 | §7 / R-10.2 | Olmayan hesap: unfreeze")
    void api_05() {
        BankApi.error(api.send("POST","/api/accounts/0000000000/unfreeze",null),404);
    }

    @Test
    @DisplayName("API-06 | §7 / R-10.2 | Olmayan hesap: close")
    void api_06() {
        BankApi.error(api.send("POST","/api/accounts/0000000000/close",null),404);
    }

    @Test
    @DisplayName("API-07 | §7 / R-10.2 | Olmayan hesap: interest")
    void api_07() {
        BankApi.error(api.send("POST","/api/accounts/0000000000/interest",null),404);
    }

    @Test
    @DisplayName("API-08 | §7 / R-10.2 | Olmayan hesap: transactions")
    void api_08() {
        BankApi.error(api.send("GET","/api/accounts/0000000000/transactions",null),404);
    }

    @Test
    @DisplayName("API-09 | API referansı | Tüm hesaplar listelenir")
    void api_09() {
        String n=api.create("STANDARD",0.0); BankApi.Reply r=api.send("GET","/api/accounts",null); BankApi.code(r,200); assertTrue(r.json().isArray()); assertTrue(java.util.stream.StreamSupport.stream(r.json().spliterator(),false).anyMatch(a->a.path("accountNumber").asText().equals(n)));
    }

    @Test
    @DisplayName("API-10 | §7 | Eksik amount reddedilir")
    void api_10() {
        String n=api.create("STANDARD",100.0); JsonNode before=api.account(n); BankApi.Reply r=api.send("POST","/api/accounts/"+n+"/deposit","{}"); assertAll(()->BankApi.error(r,400),()->api.unchanged(n,before));
    }

    @Test
    @DisplayName("API-11 | §7 | Bozuk JSON reddedilir")
    void api_11() {
        BankApi.error(api.createBody("{"),400);
    }

    @Test
    @DisplayName("API-12 | R-4.2 / G-5 | Olmayan transfer hedefi kaynak bakiyesini değiştirmez")
    void api_12() {
        String a=api.create("STANDARD",100.0); JsonNode before=api.account(a); BankApi.Reply r=api.transfer(a,"0000000000",10.0); assertAll(()->BankApi.error(r,404),()->api.unchanged(a,before));
    }

    @Test
    @DisplayName("API-13 | R-9.1 | Alternatif geçmiş yolu olmayan hesapta 404")
    void api_13() {
        BankApi.error(api.send("GET","/api/transactions/0000000000",null),404);
    }

    @Test
    @DisplayName("API-14 | API referansı | İki işlem geçmişi endpointi aynı veriyi döndürür")
    void api_14() {
        String n=api.create("STANDARD",0.0); BankApi.code(api.operation(n,"deposit",1.0),200); JsonNode expected=api.history(n); BankApi.Reply r=api.send("GET","/api/transactions/"+n,null); BankApi.code(r,200); assertEquals(expected,r.json());
    }

    @Test
    @DisplayName("API-15 | §7 / G-5 | Eksik transfer tutarı reddedilir ve hesaplar korunur")
    void api_15() {
        String a=api.create("STANDARD",100.0); String b=api.create("STANDARD",0.0); JsonNode beforeA=api.account(a); JsonNode beforeB=api.account(b); BankApi.Reply r=api.send("POST","/api/transactions/transfer",BankApi.json(Map.of("fromAccount",a,"toAccount",b))); assertAll(()->BankApi.error(r,400),()->api.unchanged(a,beforeA),()->api.unchanged(b,beforeB));
    }

}
