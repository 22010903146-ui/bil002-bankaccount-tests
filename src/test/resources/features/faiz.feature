Feature: Aylık faiz
  @hata
  Scenario Outline: Faiz kuruşa yuvarlanarak eklenir
    Given a "<type>" account with balance <initial>
    When the owner requests "interest"
    Then the response code is 200
    And the account balance is <final> TL
    And the latest transaction is "INTEREST" with status "SUCCESS"
    Examples:
      | type     | initial | final   |
      | STANDARD | 1000.0  | 1002.50 |
      | PREMIUM  | 1000.0  | 1004.17 |
  Scenario: Dondurulmuş hesaba faiz uygulanmaz
    Given a "STANDARD" account with balance 100.0
    And the account is "FROZEN"
    When the owner requests "interest"
    Then the response code is 409
    And the account is unchanged
