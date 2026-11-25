package tr.edu.sakarya.bil002.blackbox;
import tr.edu.sakarya.bil002.support.BankApi;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
public abstract class BlackBoxContract {
    private final BankApi api;
    protected BlackBoxContract(BankApi api) { this.api = api; }
    @Test
    @DisplayName("EP-01 | R-1.1 | Geçersiz hesap sahibi: None")
    void ep_01() {
        BankApi.error(api.createBody("{\"ownerName\": null, \"accountType\": \"STANDARD\", \"initialBalance\": 0}"), 400);
    }

    @Test
    @DisplayName("EP-02 | R-1.1 | Geçersiz hesap sahibi: ''")
    void ep_02() {
        BankApi.error(api.createBody("{\"ownerName\": \"\", \"accountType\": \"STANDARD\", \"initialBalance\": 0}"), 400);
    }

    @Test
    @DisplayName("EP-03 | R-1.1 | Geçersiz hesap sahibi: '   '")
    void ep_03() {
        BankApi.error(api.createBody("{\"ownerName\": \"   \", \"accountType\": \"STANDARD\", \"initialBalance\": 0}"), 400);
    }

    @Test
    @DisplayName("EP-04 | R-1.2 | Geçersiz hesap türü: None")
    void ep_04() {
        BankApi.error(api.createBody("{\"ownerName\": \"Test\", \"accountType\": null, \"initialBalance\": 0}"), 400);
    }

    @Test
    @DisplayName("EP-05 | R-1.2 | Geçersiz hesap türü: 'standard'")
    void ep_05() {
        BankApi.error(api.createBody("{\"ownerName\": \"Test\", \"accountType\": \"standard\", \"initialBalance\": 0}"), 400);
    }

    @Test
    @DisplayName("EP-06 | R-1.2 | Geçersiz hesap türü: 'BUSINESS'")
    void ep_06() {
        BankApi.error(api.createBody("{\"ownerName\": \"Test\", \"accountType\": \"BUSINESS\", \"initialBalance\": 0}"), 400);
    }

    @Test
    @DisplayName("EP-07 | R-1.3 | Null başlangıç bakiyesi sıfırdır")
    void ep_07() {
        String n = api.create("STANDARD", null); assertEquals(0.0, api.balance(n), 0.001);
    }

    @Test
    @Tag("hata") // H-01: source-confirmed; execution still required
    @DisplayName("EP-08 | R-1.4 | Negatif başlangıç bakiyesi reddedilir")
    void ep_08() {
        BankApi.error(api.createBody("{\"ownerName\":\"Test\",\"accountType\":\"STANDARD\",\"initialBalance\":-0.01}"), 400);
    }

    @Test
    @DisplayName("EP-09 | R-1.5 / R-1.6 / R-1.7 | Benzersiz numara, ACTIVE durum ve boş geçmiş")
    void ep_09() {
        Set<String> numbers = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            String n = api.create("STANDARD", 0.0);
            assertTrue(numbers.add(n), "R-1.5 duplicate number");
            assertEquals("ACTIVE", api.account(n).path("status").asText());
            assertEquals(0, api.history(n).size());
        }
    }

    @Test
    @DisplayName("BVA-01 | R-1.4 | Başlangıç bakiyesi 0.0 kabul edilir")
    void bva_01() {
        String n = api.create("STANDARD", 0.0); assertEquals(0.0, api.balance(n), 0.001);
    }

    @Test
    @DisplayName("BVA-02 | R-1.4 | Başlangıç bakiyesi 0.01 kabul edilir")
    void bva_02() {
        String n = api.create("STANDARD", 0.01); assertEquals(0.01, api.balance(n), 0.001);
    }

    @Test
    @DisplayName("BVA-03 | R-2.2 / R-3.2 | deposit tutarı -0.01")
    void bva_03() {
        String n = api.create("STANDARD", 100.0); api.rejected(n, "deposit", -0.01, 400);
    }

    @Test
    @Tag("hata") // H-02: source-confirmed; execution still required
    @DisplayName("BVA-04 | R-2.2 / R-3.2 | deposit tutarı 0")
    void bva_04() {
        String n = api.create("STANDARD", 100.0); api.rejected(n, "deposit", 0.0, 400);
    }

    @Test
    @DisplayName("BVA-05 | R-2.2 / R-3.2 | deposit tutarı 0.01")
    void bva_05() {
        String n = api.create("STANDARD", 100.0); BankApi.code(api.operation(n, "deposit", 0.01), 200); assertEquals(100.01, api.balance(n), 0.001);
    }

    @Test
    @DisplayName("BVA-06 | R-2.2 / R-3.2 | withdraw tutarı -0.01")
    void bva_06() {
        String n = api.create("STANDARD", 100.0); api.rejected(n, "withdraw", -0.01, 400);
    }

    @Test
    @DisplayName("BVA-07 | R-2.2 / R-3.2 | withdraw tutarı 0")
    void bva_07() {
        String n = api.create("STANDARD", 100.0); api.rejected(n, "withdraw", 0.0, 400);
    }

    @Test
    @DisplayName("BVA-08 | R-2.2 / R-3.2 | withdraw tutarı 0.01")
    void bva_08() {
        String n = api.create("STANDARD", 100.0); BankApi.code(api.operation(n, "withdraw", 0.01), 200); assertEquals(99.99, api.balance(n), 0.001);
    }

    @Test
    @DisplayName("BVA-09 | R-2.4 / §3 | STANDARD yatırma sınırı: 9999.99")
    void bva_09() {
        String n = api.create("STANDARD", 0.0); BankApi.code(api.operation(n, "deposit", 9999.99), 200); assertEquals(9999.99, api.balance(n), 0.001);
    }

    @Test
    @DisplayName("BVA-10 | R-2.4 / §3 | STANDARD yatırma sınırı: 10000")
    void bva_10() {
        String n = api.create("STANDARD", 0.0); BankApi.code(api.operation(n, "deposit", 10000.0), 200); assertEquals(10000.0, api.balance(n), 0.001);
    }

    @Test
    @DisplayName("BVA-11 | R-2.4 / §3 | STANDARD yatırma sınırı: 10000.01")
    void bva_11() {
        String n = api.create("STANDARD", 0.0); api.rejected(n, "deposit", 10000.01, 422);
    }

    @Test
    @DisplayName("BVA-12 | R-2.4 / §3 | PREMIUM yatırma sınırı: 49999.99")
    void bva_12() {
        String n = api.create("PREMIUM", 0.0); BankApi.code(api.operation(n, "deposit", 49999.99), 200); assertEquals(49999.99, api.balance(n), 0.001);
    }

    @Test
    @DisplayName("BVA-13 | R-2.4 / §3 | PREMIUM yatırma sınırı: 50000")
    void bva_13() {
        String n = api.create("PREMIUM", 0.0); BankApi.code(api.operation(n, "deposit", 50000.0), 200); assertEquals(50000.0, api.balance(n), 0.001);
    }

    @Test
    @DisplayName("BVA-14 | R-2.4 / §3 | PREMIUM yatırma sınırı: 50000.01")
    void bva_14() {
        String n = api.create("PREMIUM", 0.0); api.rejected(n, "deposit", 50000.01, 422);
    }

    @Test
    @Tag("hata") // H-03: source-confirmed; execution still required
    @DisplayName("BVA-15 | R-3.4 | Bakiyenin tamamı çekilebilir")
    void bva_15() {
        String n = api.create("STANDARD", 100.0); BankApi.code(api.operation(n, "withdraw", 100.0), 200); assertEquals(0.0, api.balance(n), 0.001);
    }

    @Test
    @DisplayName("BVA-16 | R-3.4 | Bakiyeyi bir kuruş aşan çekim reddedilir")
    void bva_16() {
        String n = api.create("STANDARD", 100.0); api.rejected(n, "withdraw", 100.01, 422);
    }

    @Test
    @DisplayName("BVA-17 | R-3.5 / §3 | STANDARD: birikimli günlük limit tam sınır ve sınır üstü")
    void bva_17() {
        String n = api.create("STANDARD", 5100.0);
        BankApi.code(api.operation(n, "withdraw", 4999.99), 200);
        BankApi.code(api.operation(n, "withdraw", 0.01), 200);
        assertEquals(100.0, api.balance(n), 0.001);
        api.rejected(n, "withdraw", 0.01, 422);
    }

    @Test
    @Tag("hata") // H-04: source-confirmed; execution still required
    @DisplayName("BVA-18 | R-3.5 / §3 | PREMIUM: birikimli günlük limit tam sınır ve sınır üstü")
    void bva_18() {
        String n = api.create("PREMIUM", 20100.0);
        BankApi.code(api.operation(n, "withdraw", 19999.99), 200);
        BankApi.code(api.operation(n, "withdraw", 0.01), 200);
        assertEquals(100.0, api.balance(n), 0.001);
        api.rejected(n, "withdraw", 0.01, 422);
    }

    @Test
    @DisplayName("ST-01 | §4.2 / R-5 / R-6 / R-7 | ACTIVE + freeze, bakiye=25")
    void st_01() {
        String n = api.create("STANDARD", 25.0); api.state(n, "ACTIVE"); BankApi.code(api.operation(n, "freeze", null), 200); assertEquals(25.0, api.balance(n), 0.001); assertEquals("FROZEN", api.account(n).path("status").asText());
    }

    @Test
    @DisplayName("ST-02 | §4.2 / R-5 / R-6 / R-7 | ACTIVE + unfreeze, bakiye=25")
    void st_02() {
        String n = api.create("STANDARD", 25.0); api.state(n, "ACTIVE"); api.rejectedTransition(n, "unfreeze"); assertEquals("ACTIVE", api.account(n).path("status").asText());
    }

    @Test
    @DisplayName("ST-03 | §4.2 / R-5 / R-6 / R-7 | ACTIVE + close, bakiye=0")
    void st_03() {
        String n = api.create("STANDARD", 0.0); api.state(n, "ACTIVE"); BankApi.code(api.operation(n, "close", null), 200); assertEquals(0.0, api.balance(n), 0.001); assertEquals("CLOSED", api.account(n).path("status").asText());
    }

    @Test
    @Tag("hata") // H-05: source-confirmed; execution still required
    @DisplayName("ST-04 | §4.2 / R-5 / R-6 / R-7 | ACTIVE + close, bakiye=25")
    void st_04() {
        String n = api.create("STANDARD", 25.0); api.state(n, "ACTIVE"); api.rejectedTransition(n, "close"); assertEquals("ACTIVE", api.account(n).path("status").asText());
    }

    @Test
    @Tag("hata") // H-06: source-confirmed; execution still required
    @DisplayName("ST-05 | §4.2 / R-5 / R-6 / R-7 | FROZEN + freeze, bakiye=25")
    void st_05() {
        String n = api.create("STANDARD", 25.0); api.state(n, "FROZEN"); api.rejectedTransition(n, "freeze"); assertEquals("FROZEN", api.account(n).path("status").asText());
    }

    @Test
    @DisplayName("ST-06 | §4.2 / R-5 / R-6 / R-7 | FROZEN + unfreeze, bakiye=25")
    void st_06() {
        String n = api.create("STANDARD", 25.0); api.state(n, "FROZEN"); BankApi.code(api.operation(n, "unfreeze", null), 200); assertEquals(25.0, api.balance(n), 0.001); assertEquals("ACTIVE", api.account(n).path("status").asText());
    }

    @Test
    @DisplayName("ST-07 | §4.2 / R-5 / R-6 / R-7 | FROZEN + close, bakiye=0")
    void st_07() {
        String n = api.create("STANDARD", 0.0); api.state(n, "FROZEN"); BankApi.code(api.operation(n, "close", null), 200); assertEquals(0.0, api.balance(n), 0.001); assertEquals("CLOSED", api.account(n).path("status").asText());
    }

    @Test
    @Tag("hata") // H-05: source-confirmed; execution still required
    @DisplayName("ST-08 | §4.2 / R-5 / R-6 / R-7 | FROZEN + close, bakiye=25")
    void st_08() {
        String n = api.create("STANDARD", 25.0); api.state(n, "FROZEN"); api.rejectedTransition(n, "close"); assertEquals("FROZEN", api.account(n).path("status").asText());
    }

    @Test
    @DisplayName("ST-09 | §4.2 / R-5 / R-6 / R-7 | CLOSED + freeze, bakiye=0")
    void st_09() {
        String n = api.create("STANDARD", 0.0); api.state(n, "CLOSED"); api.rejectedTransition(n, "freeze"); assertEquals("CLOSED", api.account(n).path("status").asText());
    }

    @Test
    @DisplayName("ST-10 | §4.2 / R-5 / R-6 / R-7 | CLOSED + unfreeze, bakiye=0")
    void st_10() {
        String n = api.create("STANDARD", 0.0); api.state(n, "CLOSED"); api.rejectedTransition(n, "unfreeze"); assertEquals("CLOSED", api.account(n).path("status").asText());
    }

    @Test
    @DisplayName("ST-11 | §4.2 / R-5 / R-6 / R-7 | CLOSED + close, bakiye=0")
    void st_11() {
        String n = api.create("STANDARD", 0.0); api.state(n, "CLOSED"); api.rejectedTransition(n, "close"); assertEquals("CLOSED", api.account(n).path("status").asText());
    }

    @Test
    @DisplayName("DT-01 | R-4.1–R-4.6 / G-5 | Transfer ACTIVE → ACTIVE; aynı=False; tutar=25")
    void dt_01() {
        String a = api.create("STANDARD", 100.0); String b = api.create("STANDARD", 0.0); api.state(a, "ACTIVE"); api.state(b, "ACTIVE"); BankApi.code(api.transfer(a, b, 25.0), 200); assertEquals(75.0, api.balance(a), 0.001); assertEquals(25.0, api.balance(b), 0.001);
    }

    @Test
    @Tag("hata") // H-07: source-confirmed; execution still required
    @DisplayName("DT-02 | R-4.1–R-4.6 / G-5 | Transfer ACTIVE → ACTIVE; aynı=True; tutar=25")
    void dt_02() {
        String a = api.create("STANDARD", 100.0); String b = a; api.state(a, "ACTIVE"); api.rejectedTransfer(a, b, 25.0, 400);
    }

    @Test
    @DisplayName("DT-03 | R-4.1–R-4.6 / G-5 | Transfer ACTIVE → ACTIVE; aynı=False; tutar=100.01")
    void dt_03() {
        String a = api.create("STANDARD", 100.0); String b = api.create("STANDARD", 0.0); api.state(a, "ACTIVE"); api.state(b, "ACTIVE"); api.rejectedTransfer(a, b, 100.01, 422);
    }

    @Test
    @DisplayName("DT-04 | R-4.1–R-4.6 / G-5 | Transfer FROZEN → ACTIVE; aynı=False; tutar=25")
    void dt_04() {
        String a = api.create("STANDARD", 100.0); String b = api.create("STANDARD", 0.0); api.state(a, "FROZEN"); api.state(b, "ACTIVE"); api.rejectedTransfer(a, b, 25.0, 409);
    }

    @Test
    @DisplayName("DT-05 | R-4.1–R-4.6 / G-5 | Transfer CLOSED → ACTIVE; aynı=False; tutar=0.01")
    void dt_05() {
        String a = api.create("STANDARD", 0.0); String b = api.create("STANDARD", 0.0); api.state(a, "CLOSED"); api.state(b, "ACTIVE"); api.rejectedTransfer(a, b, 0.01, 409);
    }

    @Test
    @Tag("hata") // H-08: source-confirmed; execution still required
    @DisplayName("DT-06 | R-4.1–R-4.6 / G-5 | Transfer ACTIVE → FROZEN; aynı=False; tutar=25")
    void dt_06() {
        String a = api.create("STANDARD", 100.0); String b = api.create("STANDARD", 0.0); api.state(a, "ACTIVE"); api.state(b, "FROZEN"); api.rejectedTransfer(a, b, 25.0, 409);
    }

    @Test
    @Tag("hata") // H-08: source-confirmed; execution still required
    @DisplayName("DT-07 | R-4.1–R-4.6 / G-5 | Transfer ACTIVE → CLOSED; aynı=False; tutar=25")
    void dt_07() {
        String a = api.create("STANDARD", 100.0); String b = api.create("STANDARD", 0.0); api.state(a, "ACTIVE"); api.state(b, "CLOSED"); api.rejectedTransfer(a, b, 25.0, 409);
    }

    @Test
    @DisplayName("DT-08 | R-4.1–R-4.6 / G-5 | Transfer ACTIVE → ACTIVE; aynı=False; tutar=0")
    void dt_08() {
        String a = api.create("STANDARD", 100.0); String b = api.create("STANDARD", 0.0); api.state(a, "ACTIVE"); api.state(b, "ACTIVE"); api.rejectedTransfer(a, b, 0.0, 400);
    }

    @Test
    @DisplayName("DT-09 | R-4.1–R-4.6 / G-5 | Transfer ACTIVE → ACTIVE; aynı=False; tutar=-0.01")
    void dt_09() {
        String a = api.create("STANDARD", 100.0); String b = api.create("STANDARD", 0.0); api.state(a, "ACTIVE"); api.state(b, "ACTIVE"); api.rejectedTransfer(a, b, -0.01, 400);
    }

    @Test
    @DisplayName("DT-10 | R-4.1–R-4.6 / G-5 | Transfer ACTIVE → ACTIVE; aynı=False; tutar=100")
    void dt_10() {
        String a = api.create("STANDARD", 100.0); String b = api.create("STANDARD", 0.0); api.state(a, "ACTIVE"); api.state(b, "ACTIVE"); BankApi.code(api.transfer(a, b, 100.0), 200); assertEquals(0.0, api.balance(a), 0.001); assertEquals(100.0, api.balance(b), 0.001);
    }

    @Test
    @DisplayName("DT-11 | R-4.8 | Transfer günlük çekim limitinden bağımsızdır")
    void dt_11() {
        String a = api.create("STANDARD", 11000.0); String b = api.create("STANDARD", 0.0); BankApi.code(api.operation(a, "withdraw", 5000.0), 200); BankApi.code(api.transfer(a,b,6000.0),200); assertEquals(0.0,api.balance(a),0.001); assertEquals(6000.0,api.balance(b),0.001);
    }

    @Test
    @Tag("hata") // H-09: source-confirmed; execution still required
    @DisplayName("INT-01 | R-8.2–R-8.5 | STANDARD, 1000 TL aylık faiz")
    void int_01() {
        String n = api.create("STANDARD", 1000.0);
        BankApi.code(api.operation(n, "interest", null),200);
        assertEquals(1002.5, api.balance(n),0.001);
        JsonNode history = api.history(n); assertEquals(1,history.size());
        assertEquals("INTEREST",history.get(0).path("type").asText());
        assertEquals("SUCCESS",history.get(0).path("status").asText());
        assertEquals(2.5,history.get(0).path("amount").asDouble(),0.001);
    }

    @Test
    @Tag("hata") // H-09: source-confirmed; execution still required
    @DisplayName("INT-02 | R-8.2–R-8.5 | PREMIUM, 1000 TL aylık faiz")
    void int_02() {
        String n = api.create("PREMIUM", 1000.0);
        BankApi.code(api.operation(n, "interest", null),200);
        assertEquals(1004.17, api.balance(n),0.001);
        JsonNode history = api.history(n); assertEquals(1,history.size());
        assertEquals("INTEREST",history.get(0).path("type").asText());
        assertEquals("SUCCESS",history.get(0).path("status").asText());
        assertEquals(4.17,history.get(0).path("amount").asDouble(),0.001);
    }

    @Test
    @DisplayName("INT-03 | R-8.2–R-8.5 | STANDARD, 12000 TL aylık faiz")
    void int_03() {
        String n = api.create("STANDARD", 12000.0);
        BankApi.code(api.operation(n, "interest", null),200);
        assertEquals(12030.0, api.balance(n),0.001);
        JsonNode history = api.history(n); assertEquals(1,history.size());
        assertEquals("INTEREST",history.get(0).path("type").asText());
        assertEquals("SUCCESS",history.get(0).path("status").asText());
        assertEquals(30.0,history.get(0).path("amount").asDouble(),0.001);
    }

    @Test
    @Tag("hata") // H-09: source-confirmed; execution still required
    @DisplayName("INT-04 | R-8.2–R-8.5 | PREMIUM, 2500.5 TL aylık faiz")
    void int_04() {
        String n = api.create("PREMIUM", 2500.5);
        BankApi.code(api.operation(n, "interest", null),200);
        assertEquals(2510.92, api.balance(n),0.001);
        JsonNode history = api.history(n); assertEquals(1,history.size());
        assertEquals("INTEREST",history.get(0).path("type").asText());
        assertEquals("SUCCESS",history.get(0).path("status").asText());
        assertEquals(10.42,history.get(0).path("amount").asDouble(),0.001);
    }

    @Test
    @DisplayName("INT-05 | R-8.2–R-8.5 | STANDARD, 0 TL aylık faiz")
    void int_05() {
        String n = api.create("STANDARD", 0.0);
        BankApi.code(api.operation(n, "interest", null),200);
        assertEquals(0.0, api.balance(n),0.001);
        JsonNode history = api.history(n); assertEquals(1,history.size());
        assertEquals("INTEREST",history.get(0).path("type").asText());
        assertEquals("SUCCESS",history.get(0).path("status").asText());
        assertEquals(0.0,history.get(0).path("amount").asDouble(),0.001);
    }

    @Test
    @DisplayName("ST-X01 | §4.3 | FROZEN hesapta deposit engellenir")
    void st_x01() {
        String n=api.create("STANDARD", 100.0); api.state(n,"FROZEN"); api.rejected(n,"deposit",1.0,409);
    }

    @Test
    @Tag("hata") // H-10: source-confirmed; execution still required
    @DisplayName("ST-X02 | §4.3 | FROZEN hesapta withdraw engellenir")
    void st_x02() {
        String n=api.create("STANDARD", 100.0); api.state(n,"FROZEN"); api.rejected(n,"withdraw",1.0,409);
    }

    @Test
    @DisplayName("ST-X03 | §4.3 | FROZEN hesapta interest engellenir")
    void st_x03() {
        String n=api.create("STANDARD", 100.0); api.state(n,"FROZEN"); JsonNode before=api.account(n); BankApi.Reply r=api.operation(n,"interest",null); assertAll(()->BankApi.error(r,409),()->api.unchanged(n,before));
    }

    @Test
    @DisplayName("ST-X04 | §4.3 | CLOSED hesapta deposit engellenir")
    void st_x04() {
        String n=api.create("STANDARD", 0.0); api.state(n,"CLOSED"); api.rejected(n,"deposit",1.0,409);
    }

    @Test
    @Tag("hata") // H-10: source-confirmed; execution still required
    @DisplayName("ST-X05 | §4.3 | CLOSED hesapta withdraw engellenir")
    void st_x05() {
        String n=api.create("STANDARD", 0.0); api.state(n,"CLOSED"); api.rejected(n,"withdraw",1.0,409);
    }

    @Test
    @DisplayName("ST-X06 | §4.3 | CLOSED hesapta interest engellenir")
    void st_x06() {
        String n=api.create("STANDARD", 0.0); api.state(n,"CLOSED"); JsonNode before=api.account(n); BankApi.Reply r=api.operation(n,"interest",null); assertAll(()->BankApi.error(r,409),()->api.unchanged(n,before));
    }

    @Test
    @DisplayName("HIS-01 | R-2.6 / R-3.7 / R-9.3 | Yatırma ve çekme SUCCESS kaydı, en yeni önce")
    void his_01() {
        String n=api.create("STANDARD",100.0);
        BankApi.code(api.operation(n,"deposit",20.0),200);
        BankApi.code(api.operation(n,"withdraw",5.0),200);
        JsonNode h=api.history(n); assertEquals(2,h.size());
        assertEquals("WITHDRAWAL",h.get(0).path("type").asText());
        assertEquals("DEPOSIT",h.get(1).path("type").asText());
        assertEquals(115.0,h.get(0).path("balanceAfter").asDouble(),0.001);
        assertEquals("SUCCESS",h.get(0).path("status").asText());
        assertEquals("SUCCESS",h.get(1).path("status").asText());
        assertEquals(5.0,h.get(0).path("amount").asDouble(),0.001);
        assertEquals(20.0,h.get(1).path("amount").asDouble(),0.001);
        assertTrue(java.time.LocalDateTime.parse(h.get(0).path("timestamp").asText()).compareTo(
         java.time.LocalDateTime.parse(h.get(1).path("timestamp").asText())) >= 0);
    }

    @Test
    @Tag("hata") // H-11: source-confirmed; execution still required
    @DisplayName("HIS-02 | R-2.7 / R-3.8 / §6 | Reddedilen deposit FAILED olarak kaydedilir")
    void his_02() {
        String n=api.create("STANDARD",100.0); api.rejected(n,"deposit",-0.01,400);
        JsonNode h=api.history(n); assertEquals(1,h.size());
        assertEquals("FAILED",h.get(0).path("status").asText());
        assertEquals("DEPOSIT",h.get(0).path("type").asText());
        assertEquals(100.0,h.get(0).path("balanceAfter").asDouble(),0.001);
        assertEquals(-0.01,h.get(0).path("amount").asDouble(),0.001);
    }

    @Test
    @Tag("hata") // H-11: source-confirmed; execution still required
    @DisplayName("HIS-03 | R-2.7 / R-3.8 / §6 | Reddedilen withdraw FAILED olarak kaydedilir")
    void his_03() {
        String n=api.create("STANDARD",100.0); api.rejected(n,"withdraw",0.0,400);
        JsonNode h=api.history(n); assertEquals(1,h.size());
        assertEquals("FAILED",h.get(0).path("status").asText());
        assertEquals("WITHDRAWAL",h.get(0).path("type").asText());
        assertEquals(100.0,h.get(0).path("balanceAfter").asDouble(),0.001);
        assertEquals(0.0,h.get(0).path("amount").asDouble(),0.001);
    }

    @Test
    @DisplayName("HIS-04 | R-4.7 | Transfer iki SUCCESS kaydı üretir")
    void his_04() {
        String a=api.create("STANDARD",100.0); String b=api.create("PREMIUM",0.0); BankApi.code(api.transfer(a,b,25.0),200); for(String n: new String[]{a,b}) { JsonNode h=api.history(n); assertEquals(1,h.size()); assertEquals("TRANSFER",h.get(0).path("type").asText()); assertEquals("SUCCESS",h.get(0).path("status").asText()); assertEquals(25.0,h.get(0).path("amount").asDouble(),0.001); assertEquals(api.balance(n),h.get(0).path("balanceAfter").asDouble(),0.001); }
    }

    @Test
    @DisplayName("HIS-05 | §3 / R-3.5 | Başarısız çekimler günlük limite katılmaz")
    void his_05() {
        String n=api.create("STANDARD",6000.0); api.rejected(n,"withdraw",7000.0,422); BankApi.code(api.operation(n,"withdraw",5000.0),200); assertEquals(1000.0,api.balance(n),0.001);
    }

}
