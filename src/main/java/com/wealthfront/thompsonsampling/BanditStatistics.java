package com.wealthfront.thompsonsampling;

import java.util.Map;
import java.util.Optional;

public class BanditStatistics {

  private final Map<Integer, Double> weightsByVariant;
  private final Optional<Integer> victoriousVariant;

  public BanditStatistics(
      Map<Integer, Double> armWeightsByVariant,
      Optional<Integer> victoriousVariant) {
    this.weightsByVariant = armWeightsByVariant;
    this.victoriousVariant = victoriousVariant;
  }

  public Map<Integer, Double> getWeightsByVariant() {
    return weightsByVariant;
  }

  public Optional<Integer> getVictoriousVariant() {
    return victoriousVariant;
  }

}
