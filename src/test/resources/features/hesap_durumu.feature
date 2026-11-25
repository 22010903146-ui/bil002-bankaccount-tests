Feature: Hesap durum geçişleri
  Scenario: Dondurma ve çözme bakiyeyi korur
    Given a "STANDARD" account with balance 25.0
    When the owner requests "freeze"
    Then the response code is 200
    And the account state is "FROZEN"
    And the account balance is 25.0 TL
    When the owner requests "unfreeze"
    Then the response code is 200
    And the account state is "ACTIVE"
    And the account balance is 25.0 TL
  Scenario: Kapalı hesap tekrar açılamaz
    Given a "STANDARD" account with balance 0.0
    And the account is "CLOSED"
    When the owner requests "unfreeze"
    Then the response code is 409
    And the account is unchanged
