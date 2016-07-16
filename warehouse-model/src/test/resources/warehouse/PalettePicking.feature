Feature: Picking stored palettes with fifo rule

  Scenario: pick palettes stored at single location
    Given stock of:
      | refNo  | location | amount | produced |
      | 900300 | A-31-3   | 3      | some day |
    When need to deliver 3 palettes of 900300 to customer
    And pick list is requested
    Then 3 picks of 900300 from location A-31-3 are suggested


  Scenario: pick palettes stored at multiple location
    Given stock of:
      | refNo  | location | amount | produced |
      | 900300 | A-31-3   | 1      | some day |
      | 900300 | A-32-3   | 2      | same day |
    When need to deliver 3 palettes of 900300 to customer
    And pick list is requested
    Then 1 picks of 900300 from location A-31-3 are suggested
    Then 2 picks of 900300 from location A-32-3 are suggested


  Scenario: pick oldest palettes stored at single location
    Given stock of:
      | refNo  | location | amount | produced |
      | 900300 | A-31-3   | 1      | some day |
      | 900300 | A-32-3   | 2      | next day |
    When need to deliver 2 palettes of 900300 to customer
    And pick list is requested
    Then 1 picks of 900300 from location A-31-3 are suggested
    Then 1 picks of 900300 from location A-32-3 are suggested

