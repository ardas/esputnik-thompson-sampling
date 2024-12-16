package com.wealthfront.thompsonsampling;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class WeightUpdateTest {

  @Test
  public void getters() {
    Map<Integer, Double> weightByVariantName = ImmutableMap.of(1, 0.4, 2, 0.6);
    Map<Integer, ObservedArmPerformance> performanceByVariantName = ImmutableMap.of(
        1, new ObservedArmPerformance(1, 10L, 5L),
        2, new ObservedArmPerformance(2, 8L, 5L));

    WeightUpdate weightUpdate = new WeightUpdate(weightByVariantName, Optional.of(1), performanceByVariantName);
    assertEquals(weightByVariantName, weightUpdate.getWeightByVariantName());
    assertEquals(1, weightUpdate.getMaybeWinningVariant().get().intValue());
    assertEquals(performanceByVariantName, weightUpdate.getPerformancesByVariantName());
  }

}
