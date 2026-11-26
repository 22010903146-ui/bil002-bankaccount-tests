# Statik inceleme ve gerçek yerel testlerle doğrulanan bulgular

Referans: öğretmenin `mykucukkara/bankaccount-api` reposu, `f0e9cbd1462d33dcfdc81a75b5145ef348dfe820` commit'i.

Bulgular statik inceleme ile keşfedildi ve gerçek yerel testlerle doğrulandı.
Ana grup 137 PASS; hata grubu 37 assertion failure + 2 beklenen business exception verdi.
39 kırmızı test 13 kusuru destekler. HTTP gözlemleri yerel MockMvc üzerindedir;
canlı API koşusunda 80 vaka ayrıca yürütüldü. Kanıt: execution-results.csv, execution/bug-tests/surefire/ ve execution/live-acceptance-20261125-163335/.

## H-01 — Negatif başlangıç bakiyesi kabul ediliyor

| Alan | Değer |
|---|---|
| Gereksinim | R-1.4 |
| Kaynak | [AccountService.createAccount](https://github.com/mykucukkara/bankaccount-api/blob/f0e9cbd1462d33dcfdc81a75b5145ef348dfe820/src/main/java/tr/edu/sakarya/bil002/service/AccountService.java) |
| Teknik / durum | Statik inceleme + dinamik yerel test; DOĞRULANDI |
| Etki | Kritik |
| Yeniden üretme planı | createAccount("Ali", "STANDARD", -0.01) |
| Beklenen | IllegalArgumentException / API 400; hesap oluşmamalı |
| Kodda görülen / çıkarılan | Negatif kontrolü yok; balance doğrudan Account yapıcısına aktarılıyor. AccountController create metodunda @Valid yok. |
| Yakalayacak test | WB-07 / EP-08 |
| Gerçek çalıştırma kanıtı | execution/bug-tests/surefire/ ve execution-results.csv |

## H-02 — Sıfır tutarlı yatırma kabul ediliyor

| Alan | Değer |
|---|---|
| Gereksinim | R-2.2 |
| Kaynak | [AccountService.deposit](https://github.com/mykucukkara/bankaccount-api/blob/f0e9cbd1462d33dcfdc81a75b5145ef348dfe820/src/main/java/tr/edu/sakarya/bil002/service/AccountService.java) |
| Teknik / durum | Statik inceleme + dinamik yerel test; DOĞRULANDI |
| Etki | Orta |
| Yeniden üretme planı | ACTIVE STANDARD, bakiye 100; deposit(no, 0.0) |
| Beklenen | 400; bakiye değişmeden FAILED kaydı |
| Kodda görülen / çıkarılan | Koşul amount < 0; sıfır geçiyor. DepositRequest yalnızca @NotNull kontrol ediyor. |
| Yakalayacak test | WB-13 / BVA-04 |
| Gerçek çalıştırma kanıtı | execution/bug-tests/surefire/ ve execution-results.csv |

## H-03 — Bakiyenin tamamı çekilemiyor

| Alan | Değer |
|---|---|
| Gereksinim | R-3.4 |
| Kaynak | [AccountService.ensureSufficientBalance](https://github.com/mykucukkara/bankaccount-api/blob/f0e9cbd1462d33dcfdc81a75b5145ef348dfe820/src/main/java/tr/edu/sakarya/bil002/service/AccountService.java) |
| Teknik / durum | Statik inceleme + dinamik yerel test; DOĞRULANDI |
| Etki | Orta |
| Yeniden üretme planı | ACTIVE STANDARD, bakiye 100; withdraw(no, 100.0) |
| Beklenen | 200; yeni bakiye 0 |
| Kodda görülen / çıkarılan | amount >= balance koşulu eşitliği de InsufficientBalanceException ile reddediyor. |
| Yakalayacak test | WB-24 / BVA-15 |
| Gerçek çalıştırma kanıtı | execution/bug-tests/surefire/ ve execution-results.csv |

## H-04 — PREMIUM günlük limitin kendisi reddediliyor

| Alan | Değer |
|---|---|
| Gereksinim | R-3.5 / §3 |
| Kaynak | [AccountService.checkDailyWithdrawalLimit](https://github.com/mykucukkara/bankaccount-api/blob/f0e9cbd1462d33dcfdc81a75b5145ef348dfe820/src/main/java/tr/edu/sakarya/bil002/service/AccountService.java) |
| Teknik / durum | Statik inceleme + dinamik yerel test; DOĞRULANDI |
| Etki | Orta |
| Yeniden üretme planı | PREMIUM bakiye 30000; bugün 19999.99 çekilmiş; yeni çekim 0.01 |
| Beklenen | 200; günlük toplam 20000 kabul edilir |
| Kodda görülen / çıkarılan | PREMIUM dalında total >= 20000 kontrolü var; STANDARD dalında doğru biçimde > kullanılmış. |
| Yakalayacak test | WB-28 / BVA-18 |
| Gerçek çalıştırma kanıtı | execution/bug-tests/surefire/ ve execution-results.csv |

## H-05 — Bakiyesi olan hesap kapatılabiliyor

| Alan | Değer |
|---|---|
| Gereksinim | R-7.2 |
| Kaynak | [AccountService.closeAccount](https://github.com/mykucukkara/bankaccount-api/blob/f0e9cbd1462d33dcfdc81a75b5145ef348dfe820/src/main/java/tr/edu/sakarya/bil002/service/AccountService.java) |
| Teknik / durum | Statik inceleme + dinamik yerel test; DOĞRULANDI |
| Etki | Yüksek |
| Yeniden üretme planı | ACTIVE/FROZEN, bakiye 10; closeAccount(no) |
| Beklenen | 409; durum ve bakiye korunmalı |
| Kodda görülen / çıkarılan | Yalnızca zaten CLOSED olma kontrolü var; bakiye kontrolü olmadan CLOSED atanıyor. |
| Yakalayacak test | WB-52 / ST-04 / ST-08 |
| Gerçek çalıştırma kanıtı | execution/bug-tests/surefire/ ve execution-results.csv |

## H-06 — Zaten FROZEN hesap yeniden dondurulabiliyor

| Alan | Değer |
|---|---|
| Gereksinim | R-5.1 / §4.2 |
| Kaynak | [AccountService.freezeAccount](https://github.com/mykucukkara/bankaccount-api/blob/f0e9cbd1462d33dcfdc81a75b5145ef348dfe820/src/main/java/tr/edu/sakarya/bil002/service/AccountService.java) |
| Teknik / durum | Statik inceleme + dinamik yerel test; DOĞRULANDI |
| Etki | Orta |
| Yeniden üretme planı | FROZEN hesap; freezeAccount(no) |
| Beklenen | 409; geçersiz geçiş |
| Kodda görülen / çıkarılan | Yalnızca CLOSED kontrol ediliyor; FROZEN için tekrar save ile başarılı dönüyor. |
| Yakalayacak test | WB-45 / ST-05 |
| Gerçek çalıştırma kanıtı | execution/bug-tests/surefire/ ve execution-results.csv |

## H-07 — Aynı hesaba transfer reddedilmiyor

| Alan | Değer |
|---|---|
| Gereksinim | R-4.4 |
| Kaynak | [AccountService.transfer / validateTransferParties](https://github.com/mykucukkara/bankaccount-api/blob/f0e9cbd1462d33dcfdc81a75b5145ef348dfe820/src/main/java/tr/edu/sakarya/bil002/service/AccountService.java) |
| Teknik / durum | Statik inceleme + dinamik yerel test; DOĞRULANDI |
| Etki | Yüksek |
| Yeniden üretme planı | ACTIVE hesap, bakiye 100; transfer(no, no, 25) |
| Beklenen | 400; bakiye/durum değişmeden ret |
| Kodda görülen / çıkarılan | fromAccount == toAccount kontrolü yok. Aynı nesneye düşme/ekleme ve iki SUCCESS kayıt yolu açılıyor. |
| Yakalayacak test | WB-40 / DT-02 |
| Gerçek çalıştırma kanıtı | execution/bug-tests/surefire/ ve execution-results.csv |

## H-08 — FROZEN/CLOSED hedef transfer alabiliyor

| Alan | Değer |
|---|---|
| Gereksinim | R-4.3 |
| Kaynak | [AccountService.validateTransferParties](https://github.com/mykucukkara/bankaccount-api/blob/f0e9cbd1462d33dcfdc81a75b5145ef348dfe820/src/main/java/tr/edu/sakarya/bil002/service/AccountService.java) |
| Teknik / durum | Statik inceleme + dinamik yerel test; DOĞRULANDI |
| Etki | Yüksek |
| Yeniden üretme planı | ACTIVE kaynak 100; FROZEN veya CLOSED hedef 0; transfer 25 |
| Beklenen | 409; iki hesap da korunmalı |
| Kodda görülen / çıkarılan | validateTransferParties yalnızca source.status kontrol ediyor; target.status kontrol edilmiyor. |
| Yakalayacak test | WB-38 / WB-39 / DT-06 / DT-07 |
| Gerçek çalıştırma kanıtı | execution/bug-tests/surefire/ ve execution-results.csv |

## H-09 — Faiz kesirleri tamsayı aritmetiğinde kayboluyor

| Alan | Değer |
|---|---|
| Gereksinim | R-8.2 / R-8.3 |
| Kaynak | [AccountService.calculateInterest](https://github.com/mykucukkara/bankaccount-api/blob/f0e9cbd1462d33dcfdc81a75b5145ef348dfe820/src/main/java/tr/edu/sakarya/bil002/service/AccountService.java) |
| Teknik / durum | Statik inceleme + dinamik yerel test; DOĞRULANDI |
| Etki | Kritik |
| Yeniden üretme planı | STANDARD 1000 TL için interest; ayrıca PREMIUM 1000 ve 2500.50 |
| Beklenen | Sırasıyla 2.50, 4.17, 10.42 TL faiz |
| Kodda görülen / çıkarılan | balance.longValue() * annualRate / 100 / 12 tamsayı bölmesi yapar; koddan 2, 4 ve 10 TL çıkar. |
| Yakalayacak test | WB-53–55 / INT-01 / INT-02 / INT-04 |
| Gerçek çalıştırma kanıtı | execution/bug-tests/surefire/ ve execution-results.csv |

## H-10 — FROZEN hesaptan para çekilebiliyor

| Alan | Değer |
|---|---|
| Gereksinim | R-3.3 / §4.3 |
| Kaynak | [AccountService.withdraw](https://github.com/mykucukkara/bankaccount-api/blob/f0e9cbd1462d33dcfdc81a75b5145ef348dfe820/src/main/java/tr/edu/sakarya/bil002/service/AccountService.java) |
| Teknik / durum | Statik inceleme + dinamik yerel test; DOĞRULANDI |
| Etki | Yüksek |
| Yeniden üretme planı | FROZEN STANDARD, bakiye 100, günlük toplam 0; withdraw 25 |
| Beklenen | 409; bakiye 100 korunmalı |
| Kodda görülen / çıkarılan | withdraw içinde requireActive çağrısı yok; bakiye ve günlük limit uygunken işlem tamamlanıyor. |
| Yakalayacak test | WB-30 / ST-X02 |
| Gerçek çalıştırma kanıtı | execution/bug-tests/surefire/ ve execution-results.csv |

## H-11 — Reddedilen yatırma/çekme için FAILED kayıt yok

| Alan | Değer |
|---|---|
| Gereksinim | R-2.7 / R-3.8 / §6 |
| Kaynak | [AccountService.deposit / withdraw](https://github.com/mykucukkara/bankaccount-api/blob/f0e9cbd1462d33dcfdc81a75b5145ef348dfe820/src/main/java/tr/edu/sakarya/bil002/service/AccountService.java) |
| Teknik / durum | Statik inceleme + dinamik yerel test; DOĞRULANDI |
| Etki | Düşük |
| Yeniden üretme planı | Var olan hesap; sayısal -0.01 yatırma/çekme denemesi; sonra history |
| Beklenen | FAILED kayıt; balanceAfter değişmemiş bakiye |
| Kodda görülen / çıkarılan | İstisnadan önce veya sonra başarısız kayıt yolu yok; TransactionService.record yalnızca SUCCESS oluşturuyor. |
| Yakalayacak test | HIS-02 / HIS-03 / HistoryRepositoryIntegrationTest.rejectedWithdrawalMustHaveFailedHistoryRecord |
| Gerçek çalıştırma kanıtı | execution/bug-tests/surefire/ ve execution-results.csv |

## H-12 — Olmayan hesabın sorgusu 404 yerine 200 dönüyor

| Alan | Değer |
|---|---|
| Gereksinim | R-10.2 |
| Kaynak | [AccountController.getAccount](https://github.com/mykucukkara/bankaccount-api/blob/f0e9cbd1462d33dcfdc81a75b5145ef348dfe820/src/main/java/tr/edu/sakarya/bil002/controller/AccountController.java) |
| Teknik / durum | Statik inceleme + dinamik yerel test; DOĞRULANDI |
| Etki | Orta |
| Yeniden üretme planı | GET /api/accounts/0000000000 |
| Beklenen | 404 ve hata JSON gövdesi |
| Kodda görülen / çıkarılan | Optional boşken .orElse(ResponseEntity.ok(null)) kullanılıyor. |
| Yakalayacak test | API-01 |
| Gerçek çalıştırma kanıtı | execution/bug-tests/surefire/ ve execution-results.csv |

## H-13 — Önceki günlerin çekimleri günlük toplama katılıyor

| Alan | Değer |
|---|---|
| Gereksinim | G-6 / §3 |
| Kaynak | [TransactionService.getDailyWithdrawalTotal](https://github.com/mykucukkara/bankaccount-api/blob/f0e9cbd1462d33dcfdc81a75b5145ef348dfe820/src/main/java/tr/edu/sakarya/bil002/service/TransactionService.java) |
| Teknik / durum | Statik inceleme + dinamik yerel test; DOĞRULANDI |
| Etki | Orta |
| Yeniden üretme planı | Dün SUCCESS WITHDRAWAL 100; bugün SUCCESS WITHDRAWAL 25; günlük toplam sorgusu |
| Beklenen | 25 TL; yalnızca sunucunun bugünkü kayıtları |
| Kodda görülen / çıkarılan | Repository sorgusunda tarih parametresi yok; stream de timestamp filtrelemeden tüm SUCCESS WITHDRAWAL kayıtlarını topluyor. |
| Yakalayacak test | TransactionServiceWhiteBoxTest.previousDayWithdrawalsMustNotConsumeTodaysLimit / HistoryRepositoryIntegrationTest.dailyTotalExcludesPreviousCalendarDay |
| Gerçek çalıştırma kanıtı | execution/bug-tests/surefire/ ve execution-results.csv |


## H-14 - Eşzamanlı onaylı yatırmalar son bakiyeye tam yansımıyor

| Alan | Değer |
|---|---|
| Gereksinim | G-5 / R-2.5; eşzamanlı bakiye tutarlılığı |
| Etki | Kritik |
| Ortam | Öğrencinin izole canlı servisi; 50 kullanıcı, 60 sn |
| Adımlar | Yeni sıfır bakiyeli hesap; eşzamanlı 1 TL deposit ve bakiye okuma; son bakiye kontrolü |
| Beklenen | 5071 onaylı yatırma sonrası 5071 TL |
| Gerçekleşen | 3587 TL; 1484 TL fark; belirsiz yatırma 0 |
| Kanıt | performans/sonuclar/live-20261125-163335/ledger.json ve run.jtl |
| Sınırlama | Tek koşuda tutarsızlık gözlendi; kesin kök neden için tekrar ve transaction/locking analizi gerekir |

HTTP yanıtlarının %0 hata vermesi ledger doğrulamasını geçirmez. Üretim kodu değiştirilmedi.
