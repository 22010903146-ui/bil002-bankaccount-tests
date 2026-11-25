Feature: Para yatırma kuralları
  Scenario Outline: Hesap türüne göre yatırma limiti
    Given a "<type>" account with balance 0.0
    When the owner deposits <amount> TL
    Then the response code is <code>
    And the account balance is <balance> TL
    Examples:
      | type     | amount   | code | balance |
      | STANDARD | 10000.0  | 200  | 10000.0 |
      | STANDARD | 10000.01 | 422  | 0.0     |
      | PREMIUM  | 50000.0  | 200  | 50000.0 |
      | PREMIUM  | 50000.01 | 422  | 0.0     |
  @hata
  Scenario: Sıfır tutar yatırma reddedilir
    Given a "STANDARD" account with balance 100.0
    When the owner deposits 0.0 TL
    Then the response code is 400
    And the account is unchanged
