package com.wealthfront.thompsonsampling;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class BatchedABTestTest {

  @Test
  public void getBanditStatistics_requiresMinSamples_belowMinSamples_returnsEmptyVictoriousVariant() {
    BanditPerformance performance = new BanditPerformance(Lists.newArrayList(
        new ObservedArmPerformance(1, 5L, 5L),
        new ObservedArmPerformance(2, 5L, 5L)));
    BatchedABTest batchedABTest = new BatchedABTest();
    batchedABTest.setRequiresMinSamples(true);
    BanditStatistics result = batchedABTest.getBanditStatistics(performance);
    assertFalse(batchedABTest.getBanditStatistics(performance).getVictoriousVariant().isPresent());
    assertEquals(ImmutableMap.of(1, 0.5, 2, 0.5), result.getWeightsByVariant());
  }

  @Test
  public void getBanditStatistics_chiSquaredAboveConfidenceLevel_returnsVictoriousVariant() {
    BanditPerformance performance = new BanditPerformance(Lists.newArrayList(
        new ObservedArmPerformance(1, 100L, 0L),
        new ObservedArmPerformance(2, 0L, 100L)));
    BatchedABTest batchedABTest = new BatchedABTest();
    batchedABTest.setRequiresMinSamples(false);
    BanditStatistics result = batchedABTest.getBanditStatistics(performance);
    assertEquals(1, result.getVictoriousVariant().get().intValue());
    assertEquals(ImmutableMap.of(1, 0.5, 2, 0.5), result.getWeightsByVariant());

    performance = new BanditPerformance(Lists.newArrayList(
        new ObservedArmPerformance(1, 0L, 100L),
        new ObservedArmPerformance(2, 100L, 0L)));
    batchedABTest = new BatchedABTest();
    batchedABTest.setRequiresMinSamples(false);
    assertEquals(2, batchedABTest.getBanditStatistics(performance).getVictoriousVariant().get().intValue());
    assertEquals(ImmutableMap.of(1, 0.5, 2, 0.5), result.getWeightsByVariant());
  }

  @Test
  public void getBanditStatistics_chiSquaredBelowConfidenceLevel_returnsEmptyVictoriousVariant() {
    BanditPerformance performance = new BanditPerformance(Lists.newArrayList(
        new ObservedArmPerformance(1, 5L, 5L),
        new ObservedArmPerformance(2, 5L, 5L)));
    BatchedABTest batchedABTest = new BatchedABTest();
    batchedABTest.setRequiresMinSamples(false);
    BanditStatistics result = batchedABTest.getBanditStatistics(performance);
    assertFalse(batchedABTest.getBanditStatistics(performance).getVictoriousVariant().isPresent());
    assertEquals(ImmutableMap.of(1, 0.5, 2, 0.5), result.getWeightsByVariant());
  }

  @Test
  public void getBanditStatistics_belowMinExpectedFailures_returnsEmptyVictoriousVariant() {
    BanditPerformance performance = new BanditPerformance(Lists.newArrayList(
        new ObservedArmPerformance(1, 5L, 4L),
        new ObservedArmPerformance(2, 5L, 5L)));
    BatchedABTest batchedABTest = new BatchedABTest();
    batchedABTest.setRequiresMinSamples(false);
    BanditStatistics result = batchedABTest.getBanditStatistics(performance);
    assertFalse(batchedABTest.getBanditStatistics(performance).getVictoriousVariant().isPresent());
    assertEquals(ImmutableMap.of(1, 0.5, 2, 0.5), result.getWeightsByVariant());
  }

  @Test
  public void getBanditStatistics_belowMinExpectedSuccesses_returnsEmptyVictoriousVariant() {
    BanditPerformance performance = new BanditPerformance(Lists.newArrayList(
        new ObservedArmPerformance(1, 5L, 5L),
        new ObservedArmPerformance(2, 4L, 5L)));
    BatchedABTest batchedABTest = new BatchedABTest();
    batchedABTest.setRequiresMinSamples(false);
    BanditStatistics result = batchedABTest.getBanditStatistics(performance);
    assertFalse(batchedABTest.getBanditStatistics(performance).getVictoriousVariant().isPresent());
    assertEquals(ImmutableMap.of(1, 0.5, 2, 0.5), result.getWeightsByVariant());
  }

}
