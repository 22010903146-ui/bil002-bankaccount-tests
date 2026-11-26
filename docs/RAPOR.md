# BIL 002 Dönem Projesi Test Raporu

Öğrenci Adı: **Abdulrahman Ahmed Mustafa Al Aboosi**  
Öğrenci No: **22010903146**  
Tarih: 25 Kasım 2026  
Kısım 1 Repo URL: https://github.com/22010903146-ui/bil002-bankaccount-tests  
Kısım 2 Repo URL: https://github.com/22010903146-ui/bil002-selenium-tests  

## Yürütme Özeti

BankAccount API; JUnit, Cucumber ve MockMvc ile test edildi. Checkstyle ile statik
kontrol, JaCoCo ile kod kapsamı ölçümü yapıldı. Canlı API üzerinde kabul ve JMeter
yük testleri yürütüldü. Selenium senaryoları ayrı bir projede çalıştırıldı.

| Grup | Gerçek sonuç | Kanıt |
|---|---|---|
| Banka ana grup (`excludedGroups=hata`) | 137 PASS, 0 FAIL/ERROR; 48 canlı tekrar SKIP; Maven exit 0 | execution/main-tests/ |
| Banka `hata` grubu | 37 assertion failure + 2 beklenen iş kuralı exception = 39 hata yakalama; 17 canlı tekrar SKIP | execution/bug-tests/ |
| Checkstyle | 0 ihlal, BUILD SUCCESS | execution/final-checkstyle/ |
| Service branch | **48/48 = %100** | kapsama/jacoco.csv |
| Service line / instruction | **121/121 / 622/622 = %100** | kapsama/jacoco.csv |
| TDD GREEN / REFACTOR | Her aşamada 10/10 PASS | tdd-evidence/ |
| Selenium | **14/14 PASS** | selenium-result.json / selenium-cucumber.html |

Banka için 158 JUnit vakası ve örnek dahil 18 BDD senaryo çalışması olmak üzere
176 yerel vaka yürütülmüştür: 137 başarılı, 39 kasıtlı kusuru yakalayan kırmızı.
39 kırmızı test, 39 ayrı kusur anlamına gelmez; **13 farklı kusur** raporlanmıştır.
Canlı tekrarların ortam koşulu nedeniyle atlanması, yerel vaka eksikliği değildir;
canlı kabul çalışması ayrıca 80 vaka ile tamamlanmıştır (62 PASS, 18 kusur yakalama).

Orijinal uygulama: `mykucukkara/bankaccount-api`, referans kaynak commit'i
`f0e9cbd1462d33dcfdc81a75b5145ef348dfe820`. Orijinal `src/main`, gereksinimler,
pom.xml, Checkstyle ve CI dosyaları hash karşılaştırmasıyla korunmuştur.
Üretim koduna yalnızca yeni TDD özelliğinin `summary` paketi eklenmiştir.

## KISIM 1 — BankAccount API Testleri

### 1. Statik Test Sonuçları

AccountService, TransactionService, controller'lar, modeller ve repository sorguları
incelendi. İlk keşif yöntemi kod incelemesi olan 13 kusur daha sonra gereksinime
dayalı testlerle dinamik olarak yeniden üretildi. Son Checkstyle çalışması 0 ihlal verdi.
Statik analiz aracı olarak Checkstyle kullanıldı; SonarQube kapsam dışında bırakıldı.

| Araç / teknik | Sonuç | Açıklama |
|---|---|---|
| Checkstyle | 0 ihlal | Öğretmenin orijinal yapılandırması kullanıldı |
| Kod inceleme | 13 kusur, dinamik testlerle desteklendi | STATIC_FINDINGS.md |
| Üretim/CI değişmezlik kontrolü | Başarılı | scripts/verify_production.py + PRODUCTION_BASELINE.json |

### 2. Kara Kutu Test Vakaları

Kara kutu testleri GEREKSINIMLER.md içindeki kurallara göre tasarlandı.
Beklenen sonuçlar ilgili gereksinim kodlarıyla eşleştirildi.
Her vaka yeni hesap açar; para karşılaştırmalarında 0,001 tolerans kullanılır.

#### 2.1 Denklik Sınıfı Bölümleme

| Test ID / metot | Gereksinim ve beklenen davranış | Gerçekleşen | Sonuç |
|---|---|---|---|
| EP-01 / `ep_01` | R-1.1: Geçersiz hesap sahibi: None | Beklenen sonuç sağlandı. | PASS |
| EP-02 / `ep_02` | R-1.1: Geçersiz hesap sahibi: '' | Beklenen sonuç sağlandı. | PASS |
| EP-03 / `ep_03` | R-1.1: Geçersiz hesap sahibi: '   ' | Beklenen sonuç sağlandı. | PASS |
| EP-04 / `ep_04` | R-1.2: Geçersiz hesap türü: None | Beklenen sonuç sağlandı. | PASS |
| EP-05 / `ep_05` | R-1.2: Geçersiz hesap türü: 'standard' | Beklenen sonuç sağlandı. | PASS |
| EP-06 / `ep_06` | R-1.2: Geçersiz hesap türü: 'BUSINESS' | Beklenen sonuç sağlandı. | PASS |
| EP-07 / `ep_07` | R-1.3: Null başlangıç bakiyesi sıfırdır | Beklenen sonuç sağlandı. | PASS |
| EP-08 / `ep_08` | R-1.4: Negatif başlangıç bakiyesi reddedilir | HTTP 400 beklenirken 201 döndü. | BUG_DETECTED |
| EP-09 / `ep_09` | R-1.5 / R-1.6 / R-1.7: Benzersiz numara, ACTIVE durum ve boş geçmiş | Beklenen sonuç sağlandı. | PASS |

#### 2.2 Sınır Değer Analizi

Başlangıç için −0,01 / 0 / 0,01; işlem tutarı için −0,01 / 0 / 0,01;
STANDARD yatırma için 9.999,99 / 10.000 / 10.000,01;
PREMIUM yatırma için 49.999,99 / 50.000 / 50.000,01 seçildi.
Tam bakiye ve bakiye+0,01, birikimli günlük limitin altı/eşiti/üstü ayrıca test edildi.

| Test ID / metot | Gereksinim ve beklenen davranış | Gerçekleşen | Sonuç |
|---|---|---|---|
| BVA-01 / `bva_01` | R-1.4: Başlangıç bakiyesi 0.0 kabul edilir | Beklenen sonuç sağlandı. | PASS |
| BVA-02 / `bva_02` | R-1.4: Başlangıç bakiyesi 0.01 kabul edilir | Beklenen sonuç sağlandı. | PASS |
| BVA-03 / `bva_03` | R-2.2 / R-3.2: deposit tutarı -0.01 | Beklenen sonuç sağlandı. | PASS |
| BVA-04 / `bva_04` | R-2.2 / R-3.2: deposit tutarı 0 | HTTP 400 beklenirken 200 döndü. | BUG_DETECTED |
| BVA-05 / `bva_05` | R-2.2 / R-3.2: deposit tutarı 0.01 | Beklenen sonuç sağlandı. | PASS |
| BVA-06 / `bva_06` | R-2.2 / R-3.2: withdraw tutarı -0.01 | Beklenen sonuç sağlandı. | PASS |
| BVA-07 / `bva_07` | R-2.2 / R-3.2: withdraw tutarı 0 | Beklenen sonuç sağlandı. | PASS |
| BVA-08 / `bva_08` | R-2.2 / R-3.2: withdraw tutarı 0.01 | Beklenen sonuç sağlandı. | PASS |
| BVA-09 / `bva_09` | R-2.4 / §3: STANDARD yatırma sınırı: 9999.99 | Beklenen sonuç sağlandı. | PASS |
| BVA-10 / `bva_10` | R-2.4 / §3: STANDARD yatırma sınırı: 10000 | Beklenen sonuç sağlandı. | PASS |
| BVA-11 / `bva_11` | R-2.4 / §3: STANDARD yatırma sınırı: 10000.01 | Beklenen sonuç sağlandı. | PASS |
| BVA-12 / `bva_12` | R-2.4 / §3: PREMIUM yatırma sınırı: 49999.99 | Beklenen sonuç sağlandı. | PASS |
| BVA-13 / `bva_13` | R-2.4 / §3: PREMIUM yatırma sınırı: 50000 | Beklenen sonuç sağlandı. | PASS |
| BVA-14 / `bva_14` | R-2.4 / §3: PREMIUM yatırma sınırı: 50000.01 | Beklenen sonuç sağlandı. | PASS |
| BVA-15 / `bva_15` | R-3.4: Bakiyenin tamamı çekilebilir | HTTP 200 beklenirken 422 döndü. | BUG_DETECTED |
| BVA-16 / `bva_16` | R-3.4: Bakiyeyi bir kuruş aşan çekim reddedilir | Beklenen sonuç sağlandı. | PASS |
| BVA-17 / `bva_17` | R-3.5 / §3: STANDARD: birikimli günlük limit tam sınır ve sınır üstü | Beklenen sonuç sağlandı. | PASS |
| BVA-18 / `bva_18` | R-3.5 / §3: PREMIUM: birikimli günlük limit tam sınır ve sınır üstü | HTTP 200 beklenirken 422 döndü. | BUG_DETECTED |

#### 2.3 Karar Tablosu

| Kural / ID | Kaynak | Hedef | Aynı hesap | Bakiye / tutar | Beklenen |
|---|---|---|---|---|---|
| DT-01 | ACTIVE | ACTIVE | Hayır | Yeterli, 25 | 200 |
| DT-02 | ACTIVE | ACTIVE | Evet | Yeterli, 25 | 400 |
| DT-03 | ACTIVE | ACTIVE | Hayır | Yetersiz, 100,01 | 422 |
| DT-04 | FROZEN | ACTIVE | Hayır | Yeterli, 25 | 409 |
| DT-05 | CLOSED | ACTIVE | Hayır | Bakiye 0, tutar 0,01 | Durum nedeniyle 409 |
| DT-06 | ACTIVE | FROZEN | Hayır | Yeterli, 25 | 409 |
| DT-07 | ACTIVE | CLOSED | Hayır | Yeterli, 25 | 409 |
| DT-08 | ACTIVE | ACTIVE | Hayır | Sıfır tutar | 400 |
| DT-09 | ACTIVE | ACTIVE | Hayır | Negatif tutar | 400 |
| DT-10 | ACTIVE | ACTIVE | Hayır | Tam bakiye | 200, kaynak 0 |
| DT-11 | ACTIVE | ACTIVE | Hayır | Günlük çekim limiti dolu, transfer için bakiye yeterli | 200 |

| Tür / limit kararı | Altı ve eşiti | Üstü | Testler |
|---|---|---|---|
| STANDARD yatırma | 200 | 422 | BVA-09–11 |
| PREMIUM yatırma | 200 | 422 | BVA-12–14 |
| STANDARD günlük çekim | Toplam ≤5000 kabul | 422 | BVA-17 |
| PREMIUM günlük çekim | Toplam ≤20000 kabul | 422 | BVA-18; eşitlikte H-04 bulundu |

| Test ID / metot | Gereksinim ve beklenen davranış | Gerçekleşen | Sonuç |
|---|---|---|---|
| DT-01 / `dt_01` | R-4.1–R-4.6 / G-5: Transfer ACTIVE → ACTIVE; aynı=False; tutar=25 | Beklenen sonuç sağlandı. | PASS |
| DT-02 / `dt_02` | R-4.1–R-4.6 / G-5: Transfer ACTIVE → ACTIVE; aynı=True; tutar=25 | HTTP 400 beklenirken 200 döndü. | BUG_DETECTED |
| DT-03 / `dt_03` | R-4.1–R-4.6 / G-5: Transfer ACTIVE → ACTIVE; aynı=False; tutar=100.01 | Beklenen sonuç sağlandı. | PASS |
| DT-04 / `dt_04` | R-4.1–R-4.6 / G-5: Transfer FROZEN → ACTIVE; aynı=False; tutar=25 | Beklenen sonuç sağlandı. | PASS |
| DT-05 / `dt_05` | R-4.1–R-4.6 / G-5: Transfer CLOSED → ACTIVE; aynı=False; tutar=0.01 | Beklenen sonuç sağlandı. | PASS |
| DT-06 / `dt_06` | R-4.1–R-4.6 / G-5: Transfer ACTIVE → FROZEN; aynı=False; tutar=25 | HTTP 409 beklenirken 200 döndü. | BUG_DETECTED |
| DT-07 / `dt_07` | R-4.1–R-4.6 / G-5: Transfer ACTIVE → CLOSED; aynı=False; tutar=25 | HTTP 409 beklenirken 200 döndü. | BUG_DETECTED |
| DT-08 / `dt_08` | R-4.1–R-4.6 / G-5: Transfer ACTIVE → ACTIVE; aynı=False; tutar=0 | Beklenen sonuç sağlandı. | PASS |
| DT-09 / `dt_09` | R-4.1–R-4.6 / G-5: Transfer ACTIVE → ACTIVE; aynı=False; tutar=-0.01 | Beklenen sonuç sağlandı. | PASS |
| DT-10 / `dt_10` | R-4.1–R-4.6 / G-5: Transfer ACTIVE → ACTIVE; aynı=False; tutar=100 | Beklenen sonuç sağlandı. | PASS |
| DT-11 / `dt_11` | R-4.8: Transfer günlük çekim limitinden bağımsızdır | Beklenen sonuç sağlandı. | PASS |

#### 2.4 Durum Geçişleri

Gereksinimler §4.2'nin 11 geçiş satırı ST-01–11 ile kapsandı. Başarılı geçişte
beklenen yeni durum ve korunmuş bakiye; geçersiz geçişte ret ve durum/bakiye
değişmezliği kontrol edildi. ST-04/ST-08 bakiyeli hesabın kapanmasını, ST-05
tekrar dondurmanın kabul edilmesini ortaya çıkardı. ST-X vakaları durumlara göre
para işlemlerini ayrıca sınar. H-10, birden çok ihlale dayanmadan, yeterli bakiyeli
FROZEN hesapta pozitif çekimle ayrıca WB-30 üzerinden doğrulandı.

| Test ID / metot | Gereksinim ve beklenen davranış | Gerçekleşen | Sonuç |
|---|---|---|---|
| ST-01 / `st_01` | §4.2 / R-5 / R-6 / R-7: ACTIVE + freeze, bakiye=25 | Beklenen sonuç sağlandı. | PASS |
| ST-02 / `st_02` | §4.2 / R-5 / R-6 / R-7: ACTIVE + unfreeze, bakiye=25 | Beklenen sonuç sağlandı. | PASS |
| ST-03 / `st_03` | §4.2 / R-5 / R-6 / R-7: ACTIVE + close, bakiye=0 | Beklenen sonuç sağlandı. | PASS |
| ST-04 / `st_04` | §4.2 / R-5 / R-6 / R-7: ACTIVE + close, bakiye=25 | HTTP 409 beklenirken 200 döndü. | BUG_DETECTED |
| ST-05 / `st_05` | §4.2 / R-5 / R-6 / R-7: FROZEN + freeze, bakiye=25 | HTTP 409 beklenirken 200 döndü. | BUG_DETECTED |
| ST-06 / `st_06` | §4.2 / R-5 / R-6 / R-7: FROZEN + unfreeze, bakiye=25 | Beklenen sonuç sağlandı. | PASS |
| ST-07 / `st_07` | §4.2 / R-5 / R-6 / R-7: FROZEN + close, bakiye=0 | Beklenen sonuç sağlandı. | PASS |
| ST-08 / `st_08` | §4.2 / R-5 / R-6 / R-7: FROZEN + close, bakiye=25 | HTTP 409 beklenirken 200 döndü. | BUG_DETECTED |
| ST-09 / `st_09` | §4.2 / R-5 / R-6 / R-7: CLOSED + freeze, bakiye=0 | Beklenen sonuç sağlandı. | PASS |
| ST-10 / `st_10` | §4.2 / R-5 / R-6 / R-7: CLOSED + unfreeze, bakiye=0 | Beklenen sonuç sağlandı. | PASS |
| ST-11 / `st_11` | §4.2 / R-5 / R-6 / R-7: CLOSED + close, bakiye=0 | Beklenen sonuç sağlandı. | PASS |
| ST-X01 / `st_x01` | §4.3: FROZEN hesapta deposit engellenir | Beklenen sonuç sağlandı. | PASS |
| ST-X02 / `st_x02` | §4.3: FROZEN hesapta withdraw engellenir | HTTP 409 beklenirken 200 döndü. | BUG_DETECTED |
| ST-X03 / `st_x03` | §4.3: FROZEN hesapta interest engellenir | Beklenen sonuç sağlandı. | PASS |
| ST-X04 / `st_x04` | §4.3: CLOSED hesapta deposit engellenir | Beklenen sonuç sağlandı. | PASS |
| ST-X05 / `st_x05` | §4.3: CLOSED hesapta withdraw engellenir | HTTP 409 beklenirken 422 döndü. | BUG_DETECTED |
| ST-X06 / `st_x06` | §4.3: CLOSED hesapta interest engellenir | Beklenen sonuç sağlandı. | PASS |

#### 2.5 Faiz ve Geçmiş

| Test ID / metot | Gereksinim ve beklenen davranış | Gerçekleşen | Sonuç |
|---|---|---|---|
| INT-01 / `int_01` | R-8.2–R-8.5: STANDARD, 1000 TL aylık faiz | Beklenen 1002.5; gözlenen 1002.0. | BUG_DETECTED |
| INT-02 / `int_02` | R-8.2–R-8.5: PREMIUM, 1000 TL aylık faiz | Beklenen 1004.17; gözlenen 1004.0. | BUG_DETECTED |
| INT-03 / `int_03` | R-8.2–R-8.5: STANDARD, 12000 TL aylık faiz | Beklenen sonuç sağlandı. | PASS |
| INT-04 / `int_04` | R-8.2–R-8.5: PREMIUM, 2500.5 TL aylık faiz | Beklenen 2510.92; gözlenen 2510.5. | BUG_DETECTED |
| INT-05 / `int_05` | R-8.2–R-8.5: STANDARD, 0 TL aylık faiz | Beklenen sonuç sağlandı. | PASS |

| Test ID / metot | Gereksinim ve beklenen davranış | Gerçekleşen | Sonuç |
|---|---|---|---|
| HIS-01 / `his_01` | R-2.6 / R-3.7 / R-9.3: Yatırma ve çekme SUCCESS kaydı, en yeni önce | Beklenen sonuç sağlandı. | PASS |
| HIS-02 / `his_02` | R-2.7 / R-3.8 / §6: Reddedilen deposit FAILED olarak kaydedilir | Beklenen kayıt sayısı 1; gözlenen 0. | BUG_DETECTED |
| HIS-03 / `his_03` | R-2.7 / R-3.8 / §6: Reddedilen withdraw FAILED olarak kaydedilir | Beklenen kayıt sayısı 1; gözlenen 0. | BUG_DETECTED |
| HIS-04 / `his_04` | R-4.7: Transfer iki SUCCESS kaydı üretir | Beklenen sonuç sağlandı. | PASS |
| HIS-05 / `his_05` | §3 / R-3.5: Başarısız çekimler günlük limite katılmaz | Beklenen sonuç sağlandı. | PASS |

### 3. Beyaz Kutu ve Kod Kapsamı

AccountService için 60, TransactionService için 5 beyaz kutu testi vardır.
AccountService gerçek nesnesi, Mockito repository/TransactionService bağımlılıklarıyla
sınanmıştır. Tarih/tür/status ayrımı ayrıca gerçek Spring/H2 repository testleriyle sınanmıştır.

| Sınıf | Kapsanan / toplam dal | Instruction | Satır |
|---|---:|---:|---:|
| AccountService | **48/48 (%100)** | 556/556 | 107/107 |
| TransactionService | Dal yok (0/0) | 66/66 | 14/14 |
| Service toplam | **48/48 (%100)** | **622/622 (%100)** | **121/121 (%100)** |

Hedef ≥%80 sağlanmıştır. Mevcut service dallarında kapsanmayan dal kalmamıştır.
JaCoCo sonuçları dal, komut ve satır kapsamı olarak ayrı ayrı verilmiştir.
Eksik bir iş kuralı kaynakta hiç if kolu oluşturmamış olabilir: %100 kapsam,
uygulamanın doğru olduğunu göstermez; 13 kusur buna rağmen bulunmuştur.

| Kaynak yolu | Testler |
|---|---|
| create owner/type/null-balance | WB-01–07 |
| find/get/list | WB-08–10 |
| deposit amount/status/type-limit | WB-11–19 |
| withdraw amount/balance/daily-limit | WB-20–30 |
| transfer amount/missing parties/status/balance | WB-31–42 |
| freeze/unfreeze/close | WB-43–52 |
| interest type/status/zero/fraction | WB-53–58 |
| history existence/frozen query | WB-59–60 |
| record/history/daily sum/date | TransactionServiceWhiteBoxTest; HistoryRepositoryIntegrationTest |

[JaCoCo HTML raporu](kapsama/html/index.html) · [CSV](kapsama/jacoco.csv).
Rapor görselleri: [JaCoCo](img/jacoco-service.png) ve
[Selenium](img/selenium-cucumber.png). Ölçümün birincil kanıtı CSV/XML ve HTML'dir.

### 4. TDD Uygulaması

Kod geliştirme sırası özgün Git geçmişinden korunmuştur. Eski rapor taslakları geçmişten ayrıldığı için commit kimlikleri değişmiştir; eşleme TDD_COMMIT_MAP.json dosyasındadır.

Yeni özellik: `GET /api/accounts/{no}/summary`.
`totalDeposited` SUCCESS DEPOSIT toplamı, `totalWithdrawn` SUCCESS WITHDRAWAL toplamı;
`transactionCount` tüm SUCCESS/FAILED kayıt sayısıdır. Başlangıç bakiyesi kayıt değildir;
faiz ve transferler nakit yatırma/çekme toplamlarına katılmaz. Bu tanımlar özet
endpoint'inin kapsamını belirler. BigDecimal kullanılır; geçmiş/bakiye değiştirilmez.
OriginalSummaryHistoryReader gerçek AccountService.getTransactionHistory metoduna bağlıdır.
Var olmayan hesap mevcut exception eşlemesiyle 404 döner; FROZEN hesabın özeti okunabilir.

| Aşama | Gerçek commit | Sonuç / kanıt |
|---|---|---|
| RED | `db4c1a8864aab44f78bdf4277df4ec7611cdddff` | 2 PASS, 8 beklenen kırmızı; [çıktı](tdd-evidence/red/maven-output.txt) |
| GREEN | `5e6d41622dc1f3bec6549d653cf7098af9b70019` | 10/10 PASS; [çıktı](tdd-evidence/green/maven-output.txt) |
| REFACTOR | `a9d486fbbbe3623a76952c1b4f41fa6012d228ae` | 10/10 PASS; [çıktı](tdd-evidence/refactor/maven-output.txt) |

RED'de 10 testten 2'si geçti; 1 assertion failure ve 7 error eksik hesaplayıcıdan
kaynaklandı. GREEN ve REFACTOR ayrı ayrı 10/10 geçti. İlk yerel GREEN denemesindeki
JVM agent attach kısıtı, test kapsamını azaltmadan Mockito subclass mock maker ile
giderildi. Mevcut banka kusurları düzeltilmedi.

### 5. BDD / Cucumber

| Feature | Açılmış senaryo | PASS | Beklenen hata yakalama | Konu |
|---|---:|---:|---:|---|
| para_yatirma.feature | 5 | 4 | 1 | Tür limitleri, sıfır tutar |
| para_cekme.feature | 2 | 1 | 1 | Tam bakiye, birikimli limit |
| transfer.feature | 2 | 2 | 0 | Tam aktarım, atomik ret |
| hesap_durumu.feature | 2 | 2 | 0 | Freeze/unfreeze, kapalı hesap |
| faiz.feature | 3 | 1 | 2 | Oran/yuvarlama, FROZEN ret |
| islem_gecmisi.feature | 2 | 1 | 1 | Açılış kaydı ve FAILED geçmiş |

Yeni feature sayısı 6, yeni senaryo çalışması 16'dır. Şablonun 2 örnek senaryosu da
geçmiştir, ancak minimum feature sayısına dahil edilmemiştir. Step cümleleri tekil;
orijinal Cucumber runner ve örnek adımları korunmuştur. MockMvc için tek Spring context
yapılandırmasına @AutoConfigureMockMvc eklenmiştir. Hata etiketi bütün feature'a değil,
yalnızca hatayı yakalayan senaryo/outline'a verilmiştir.

### 6. Entegrasyon ve API Testleri

MockMvc üzerinden gerçek controller/service/repository zinciri ve H2 kullanıldı.
80 sözleşme vakası ayrıca öğrencinin izole canlı örneğinde HTTP ile çalıştırıldı.
Sonuç: **62 PASS, 18 failure, 0 error, 0 skipped**. Maven exit 1,
18 testin uygulamadaki gereksinim ihlallerini yakalamasından kaynaklandı.

Canlı kanıt: `execution/live-acceptance-20261125-163335/`.
Özel örnek kimliği yayımlanan dosyalarda PRIVATE_INSTANCE ile maskelenmiştir.
Postman koleksiyonunda istekler arası değişken aktarımı ve pm.test doğrulamaları
bulunur. Koleksiyon Postman/Newman üzerinde çalıştırılmadı. Canlı kabul sonuçları
Java HTTP testlerinin XML çıktılarında yer alır.

| Test ID / metot | Gereksinim ve beklenen davranış | Gerçekleşen | Sonuç |
|---|---|---|---|
| API-01 / `api_01` | §7 / R-10.2: Olmayan hesap: get | HTTP 404 beklenirken 200 döndü. | BUG_DETECTED |
| API-02 / `api_02` | §7 / R-10.2: Olmayan hesap: deposit | Beklenen sonuç sağlandı. | PASS |
| API-03 / `api_03` | §7 / R-10.2: Olmayan hesap: withdraw | Beklenen sonuç sağlandı. | PASS |
| API-04 / `api_04` | §7 / R-10.2: Olmayan hesap: freeze | Beklenen sonuç sağlandı. | PASS |
| API-05 / `api_05` | §7 / R-10.2: Olmayan hesap: unfreeze | Beklenen sonuç sağlandı. | PASS |
| API-06 / `api_06` | §7 / R-10.2: Olmayan hesap: close | Beklenen sonuç sağlandı. | PASS |
| API-07 / `api_07` | §7 / R-10.2: Olmayan hesap: interest | Beklenen sonuç sağlandı. | PASS |
| API-08 / `api_08` | §7 / R-10.2: Olmayan hesap: transactions | Beklenen sonuç sağlandı. | PASS |
| API-09 / `api_09` | API referansı: Tüm hesaplar listelenir | Beklenen sonuç sağlandı. | PASS |
| API-10 / `api_10` | §7: Eksik amount reddedilir | Beklenen sonuç sağlandı. | PASS |
| API-11 / `api_11` | §7: Bozuk JSON reddedilir | Beklenen sonuç sağlandı. | PASS |
| API-12 / `api_12` | R-4.2 / G-5: Olmayan transfer hedefi kaynak bakiyesini değiştirmez | Beklenen sonuç sağlandı. | PASS |
| API-13 / `api_13` | R-9.1: Alternatif geçmiş yolu olmayan hesapta 404 | Beklenen sonuç sağlandı. | PASS |
| API-14 / `api_14` | API referansı: İki işlem geçmişi endpointi aynı veriyi döndürür | Beklenen sonuç sağlandı. | PASS |
| API-15 / `api_15` | §7 / G-5: Eksik transfer tutarı reddedilir ve hesaplar korunur | Beklenen sonuç sağlandı. | PASS |

### 7. Performans Testi

| Senaryo | Kullanıcı | Süre | Ortalama ms | P90 ms | İstek/sn | Hata % |
|---|---:|---:|---:|---:|---:|---:|
| Eşzamanlı deposit + bakiye | 50 | 60 sn | 180.697 | 238.0 | 168.36 | 0.0 |

JMX: `performans/bankaccount-yuk-testi.jmx`. Gerçek ölçüm 10110 deposit/balance
örneğini kapsar; setup/teardown istekleri bu metriklerden ayrılır. P90 nearest-rank,
throughput gerçek örnek penceresi (60.05 sn) üzerinden hesaplandı.

**H-14: yük altında bakiye tutarsızlığı gözlendi.** Başlangıç 0 TL; her yatırma 1 TL.
5071 yatırma 200 yanıtıyla onaylandı; beklenen bakiye 5071 TL,
son okuma 3587.0 TL. Belirsiz yatırma sayısı 0; ledger kontrolü **FAIL**.
HTTP hata oranının %0 olması iş kuralı tutarlılığının geçtiği anlamına gelmez.
Bu koşu gözlenen tutarsızlığı kanıtlar; kesin kök neden ve dağılım için ek tekrar gerekir.
Üretim kodunda düzeltme yapılmadı.

Gerçek kanıt: `performans/sonuclar/live-20261125-163335/metrics.json`, `ledger.json`,
`run.jtl`, `html/index.html`; örnek kimliği maskelenmiştir.

### 8. CI/CD

Öğretmenin banka pipeline'ı byte/hash kontrolüyle korunmuştur.
Yeni banka ve Selenium depolarının CI sonuçları yeniden yayınlama sonrasında bu bölüme eklenecektir.

Önceki yayınlamaya ait sonuçlar arşivlenmiş yürütme kanıtlarıdır; güncel depo CI sonucu olarak değerlendirilmez.

### 9. Bulunan Hatalar

13 kusurun tamamı statik incelemenin ardından gerçek yerel testlerle doğrulanmıştır.
Tam gereksinim kodu, adımlar, beklenen/gerçekleşen, etki ve test eşlemeleri
[STATIC_FINDINGS.md](STATIC_FINDINGS.md) içindedir. Gerçek çıktılar
[execution-results.csv](execution-results.csv) ve `execution/bug-tests/surefire/` altındadır.
Bu gruptaki 2 ERROR, beklenmeyen altyapı hatası değil; izin verilmesi gereken
tam çekim ve PREMIUM tam limit işleminde uygulamanın attığı iş kuralı exception'larıdır.

| Hata No | Açıklama | Yerel kanıt |
|---|---|---|
| H-01 | Negatif başlangıç bakiyesi kabul ediliyor | EP-08; DİNAMİK DOĞRULANDI |
| H-02 | Sıfır tutarlı yatırma kabul ediliyor | BVA-04; DİNAMİK DOĞRULANDI |
| H-03 | Bakiyenin tamamı çekilemiyor | BVA-15; DİNAMİK DOĞRULANDI |
| H-04 | PREMIUM günlük limitin kendisi reddediliyor | BVA-18; DİNAMİK DOĞRULANDI |
| H-05 | Bakiyesi olan hesap kapatılabiliyor | ST-04, ST-08; DİNAMİK DOĞRULANDI |
| H-06 | Zaten FROZEN hesap yeniden dondurulabiliyor | ST-05; DİNAMİK DOĞRULANDI |
| H-07 | Aynı hesaba transfer reddedilmiyor | DT-02; DİNAMİK DOĞRULANDI |
| H-08 | FROZEN/CLOSED hedef transfer alabiliyor | DT-06, DT-07; DİNAMİK DOĞRULANDI |
| H-09 | Faiz kesirleri tamsayı aritmetiğinde kayboluyor | INT-01, INT-02, INT-04; DİNAMİK DOĞRULANDI |
| H-10 | FROZEN hesaptan para çekilebiliyor | ST-X02, ST-X05; DİNAMİK DOĞRULANDI |
| H-11 | Reddedilen yatırma/çekme için FAILED kayıt yok | HIS-02, HIS-03; DİNAMİK DOĞRULANDI |
| H-12 | Olmayan hesabın sorgusu 404 yerine 200 dönüyor | API-01; DİNAMİK DOĞRULANDI |
| H-13 | Önceki günlerin çekimleri günlük toplama katılıyor | Önceki gün: TransactionServiceWhiteBoxTest + HistoryRepositoryIntegrationTest; DİNAMİK DOĞRULANDI |

Hesap numarası üretiminde çakışma olasılığı ayrıca incelendi. Testlerde çakışma
oluşmadığından bu konu doğrulanmış hata listesine alınmadı.
Yük testinde gözlenen H-14 bakiye tutarsızlığı Bölüm 7'de açıklanmıştır.

### 10. Sonuç ve Değerlendirme

EP geçersiz sınıfları, BVA kuruş/limit eşitliklerini, karar tablosu taraf koşullarını,
durum geçişi hesap yaşam döngüsünü, beyaz kutu ise gerçek kod yollarını hedefledi.
H-03/H-04 özellikle sınır testlerinde; H-05/H-06 durum geçişinde; H-07/H-08 karar
tablosunda; H-13 tarih kontrollü beyaz kutu/repository testinde görünür hâle geldi.
BDD aynı kusurların iş diliyle yeniden üretimini sağladı. H-12 HTTP katmanına özgüdür.

Yalnızca normal akışlar denenseydi sıfır/tam limit ve geçmiş gün sorunları kaçabilirdi.
%100 service kapsamına rağmen kusur bulunması, kapsamın bir doğruluk garantisi
olmadığını gösterdi. Yük testinde 5071 TL beklenirken 3587 TL okunması,
fonksiyonel testlerin yanında eşzamanlılık kontrolünün de gerekli olduğunu gösterdi.
Çalışma tekrarlansaydı yük testine farklı kullanıcı sayıları ve tekrarlı koşular eklenirdi.

## KISIM 2 — Selenium + Cucumber

### K2.1 Seçilen Uygulama

https://the-internet.herokuapp.com — ödevde önerilen açık demo uygulaması.
Giriş/secure/logout, bağımsız checkbox'lar ve dropdown işlevleri test edildi.
Uygulamanın tomsmith demo hesabıyla 14 fonksiyonel senaryo çalıştırıldı.
Yük testi yalnızca BankAccount API'nin izole öğrenci örneğinde yürütüldü.

### K2.2 Senaryo Tablosu

| Feature | Tanım / açılmış çalışma | Pozitif/negatif | Outline | Gerçek sonuç |
|---|---|---|---|---|
| giris.feature | 4 / 7 | İkisi de | 4 örnek | 7 PASS |
| onay_kutulari.feature | 3 / 3 | Pozitif | Hayır | 3 PASS |
| acilir_liste.feature | 3 / 4 | Seçim ve geçersiz placeholder | 2 örnek | 4 PASS |

Toplam 3 feature, 10 senaryo tanımı, 14 başarılı çalışma; her senaryoda assertion vardır.

### K2.3 Page Object Model

| Sınıf | Sorumluluk |
|---|---|
| BasePage | Görünürlük / tıklanabilirlik beklemeleri |
| LoginPage | Giriş alanları, hata mesajı, redirect |
| SecurePage | Güvenli alan başlığı ve çıkış |
| CheckboxesPage | Kutuların bağımsız okunması ve değiştirilmesi |
| DropdownPage | Seçim, son etiket ve disabled placeholder |
| TestContext / Hooks | Senaryo başına tarayıcı, failure screenshot, finally quit |

Stepdefs içinde locator veya driver.findElement yoktur. PicoContainer her senaryoya
yeni TestContext verir; önceki senaryonun oturumu/durumu sızmaz.

### K2.4 Bekleme Stratejisi

Implicit wait 0, yapılandırılabilir WebDriverWait 30 sn (CI 45 sn), page load timeout 30 sn. Görünürlük,
tıklanabilirlik, URL yönlendirmesi ve seçim güncellemesi beklenir. Thread.sleep yoktur.

### K2.5 Sorunlar ve Çözümler

Sabit zamanlamaya bağımlılık explicit wait ile; locator bakım yükü POM ile;
oturum sızıntısı yeni browser ile ele alındı. Gerçek UI koşusunda 14 senaryo da geçti;
ilk uzak koşudaki logout timeout kaydı korunmuş ve explicit wait güncellenmiştir.
Geliştirme ortamında Homebrew macOS sürümünü tanıyamadığı için checksum doğrulanan
resmi taşınabilir Temurin/Maven dağıtımları kullanıldı.

### K2.6 Gerçek Çalıştırma Sonucu

`mvn test -Dheadless=true`: exit 0, 14 test, 0 failure, 0 error, 0 skipped.
[Gerçek Cucumber HTML raporu](selenium-cucumber.html) ve [sonuç JSON](selenium-result.json).
Yeni Selenium deposunun CI sonucu yeniden yayınlama sonrasında eklenecektir.

## Kaynaklar

- BIL 002 dönem projesi [öğrenci rehberi](ODEV_REHBERI.md).
- BankAccount API [gereksinim belgesi](GEREKSINIMLER.md) ve [API tanımı](API.md).
- BIL 002 Yazılım Doğrulama ve Sınama ders notları, 1-8. haftalar.
- Orijinal öğretmen reposu: https://github.com/mykucukkara/bankaccount-api
- Selenium: https://www.selenium.dev/documentation/webdriver/waits/
- Cucumber: https://cucumber.io/docs/cucumber/api/
