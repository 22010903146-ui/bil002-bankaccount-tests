Feature: Hesaplar arası transfer
  Scenario: Bakiyenin tamamı aktarılabilir
    Given a "STANDARD" account with balance 100.0
    And an active target account with balance 0.0
    When the owner transfers 100.0 TL to the target
    Then the response code is 200
    And the account balance is 0.0 TL
    And the target balance is 100.0 TL
  Scenario: Yetersiz bakiye iki hesabı da korur
    Given a "STANDARD" account with balance 100.0
    And an active target account with balance 50.0
    When the owner transfers 100.01 TL to the target
    Then the response code is 422
    And the account is unchanged
    And the target balance is 50.0 TL
