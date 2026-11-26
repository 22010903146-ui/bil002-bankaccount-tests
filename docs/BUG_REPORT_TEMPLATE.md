# Kanıtlı hata kaydı şablonu

Bu bir bulgu değildir. Gerçek test çalışması ve doğrulamadan sonra doldurulur.

| Alan | Değer |
|---|---|
| Hata No | H-xx |
| Açıklama | Gerçek gözlem |
| İhlal edilen gereksinim | R-x.y / G-x / bölüm |
| Önkoşul | Yeni hesap, tür, bakiye, durum |
| Yeniden üretme | Sıralı gerçek istekler |
| Beklenen | Gereksinimdeki sonuç |
| Gerçekleşen | HTTP yanıtı / bakiye / kayıt |
| Test | Sınıf.metot veya feature + senaryo |
| Teknik | EP / BVA / karar / durum / beyaz kutu / BDD / API / performans |
| Etki | Kritik / Yüksek / Orta / Düşük |
| Ortam ve sürüm | Yerel / canlı, gerçek commit, saat |
| Kanıt | Kişisel token içermeyen çıktı/görsel |

Bir bağlantı hatası tek başına ürün hatası değildir. Testin kendisinin doğru olduğunu
ve davranışın gerçek gereksinimi ihlal ettiğini inceleyin; sonra yalnızca ilgili testi etiketleyin.
