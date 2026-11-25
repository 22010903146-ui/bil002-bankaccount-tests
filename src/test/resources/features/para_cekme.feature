Feature: Para çekme kuralları
  @hata
  Scenario: Bakiyenin tamamı çekilebilir
    Given a "STANDARD" account with balance 100.0
    When the owner withdraws 100.0 TL
    Then the response code is 200
    And the account balance is 0.0 TL
  Scenario: Birikimli günlük limit aşılmaz
    Given a "STANDARD" account with balance 6000.0
    When the owner withdraws 4999.99 TL
    Then the response code is 200
    When the owner withdraws 0.01 TL
    Then the response code is 200
    When the owner withdraws 0.01 TL
    Then the response code is 422
    And the account is unchanged
