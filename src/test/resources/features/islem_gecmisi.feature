Feature: İşlem geçmişi
  Scenario: Hesap açılışı kayıt üretmez
    Given a "STANDARD" account with balance 100.0
    Then the transaction history has 0 entries
  @hata
  Scenario: Başarısız çekim FAILED olarak görünür
    Given a "STANDARD" account with balance 100.0
    When the owner withdraws 100.01 TL
    Then the response code is 422
    And the transaction history has 1 entries
    And the latest transaction is "WITHDRAWAL" with status "FAILED"
