Feature: Completing of new palette

  Scenario: new palette completed in production
    # once upon a time
    When label for new palette is printed
    # some time later
    # palette is stretched
    When box is scanned
    When box is scanned
    When box is scanned
    # palette label is glued to palette
    When palette label is scanned
    Then new palette is ready to store
    Then preferred location is proposed


  Scenario: new palette completed in production (label is printed online)
    # palette is stretched
    When box is scanned
    When box is scanned
    When box is scanned
    When label for new palette is printed
    # palette label is glued to palette
    When palette label is scanned
    Then new palette is ready to store
    Then preferred location is proposed


  Scenario: new palette completed in production, but we don't scan all boxes
    # once upon a time
    When label for new palette is printed
    # some time later
    # palette is stretched
    When box is scanned
    When box amount 20
    # palette label is glued to palette
    When palette label is scanned
    Then new palette is ready to store
    Then preferred location is proposed


#  Scenario: new palette with partial box is completed in production
#    # once upon a time
#    When label for new palette is printed
#    # some time later
#    # palette is stretched
#    When box is scanned
#    When box amount 20
#    When partial box is scanned
#    # palette label is glued to palette
#    When palette label is scanned
#    Then new palette is ready to store
#    Then preferred location is proposed

