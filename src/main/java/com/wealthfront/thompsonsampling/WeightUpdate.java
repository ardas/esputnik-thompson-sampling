package com.wealthfront.thompsonsampling;

import java.util.Map;
import java.util.Optional;

public class WeightUpdate {

  private final Map<Integer, Double> weightByVariantName;
  private final Optional<Integer> maybeWinningVariant;
  private final Map<Integer, ObservedArmPerformance> performancesByVariantName;

  public WeightUpdate(
      Map<Integer, Double> weightByVariantName,
      Optional<Integer> maybeWinningVariant,
      Map<Integer, ObservedArmPerformance> performancesByVariantName) {
    this.weightByVariantName = weightByVariantName;
    this.maybeWinningVariant = maybeWinningVariant;
    this.performancesByVariantName = performancesByVariantName;
  }

  public Map<Integer, Double> getWeightByVariantName() {
    return weightByVariantName;
  }

  public Optional<Integer> getMaybeWinningVariant() {
    return maybeWinningVariant;
  }

  public Map<Integer, ObservedArmPerformance> getPerformancesByVariantName() {
    return performancesByVariantName;
  }

}
