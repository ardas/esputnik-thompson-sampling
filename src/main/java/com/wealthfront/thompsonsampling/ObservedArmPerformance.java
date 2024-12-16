package com.wealthfront.thompsonsampling;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class ObservedArmPerformance {

  private int variantId;
  private long successes;
  private long failures;


  public String getVariantName() {
    return String.valueOf(variantId);
  }

  public long getTotal() {
    return successes + failures;
  }

  public ObservedArmPerformance add(ObservedArmPerformance that) {
    if (that.getVariantId() != variantId) {
      throw new IllegalArgumentException(String.format(
          "Cannot add performance of different variant %s! Existing variant: %s",
          that.getVariantName(), variantId));
    }
    successes = successes + that.successes;
    failures = failures + that.failures;
    return this;
  }

  public double getExpectedConversionRate() {
    if (successes + failures == 0) {
      return 0.0;
    }
    return (double) successes / (successes + failures);
  }


}
