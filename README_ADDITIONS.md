# Öğrenci test çalışması — 22010903146

- Yerel ana grup: 137 PASS, 0 failure/error; 48 koşullu canlı tekrar SKIP.
- Hata grubu: 39 doğrulanmış kırmızı sonuç (37 assertion failure, 2 business exception); 17 canlı tekrar SKIP.
- 39 test 13 farklı uygulama kusurunu destekler.
- Service branch: 48/48 = %100. Checkstyle: 0 ihlal.
- Gerçek RED/GREEN/REFACTOR geçmişi ve çıktıları docs/tdd-evidence altında.
- Yeni özellik: GET /api/accounts/{no}/summary, gerçek servis adapteriyle.
- Selenium ayrı projede 14/14 başarılı.
- Canlı API: 80 vaka; 62 PASS, 18 gereksinim ihlali.
- JMeter: 50 kullanıcı, 60 saniye; sonuçlar performans/sonuclar/ altında.
- Her iki public reponun GitHub Actions çalışması başarılıdır.

Rapor: docs/RAPOR.md. Gerçek test sonuçları: docs/execution-results.csv.

```bash
mvn checkstyle:check
mvn test -DexcludedGroups=hata
mvn test -Dgroups=hata
mvn jacoco:report
python3 scripts/verify_production.py
```

Hata grubu uygulamadaki kasıtlı kusurlarda kırmızı beklenir. Orijinal üretim
kodu düzeltilmedi; CI ve gereksinimler korunmuştur. Live testler BANK_BASE_URL
yoksa koşullu atlanır. Token içeren URL'yi repoya kaydetmeyin.
