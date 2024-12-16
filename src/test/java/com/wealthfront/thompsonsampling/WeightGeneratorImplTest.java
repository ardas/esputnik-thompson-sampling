package com.wealthfront.thompsonsampling;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class WeightGeneratorImplTest {

  private final BatchedBandit bandit = mock(BatchedBandit.class);

  @Test
  public void getWeightUpdate_emptyList() {
    when(bandit.getBanditStatistics(new BanditPerformance(emptyList())))
        .thenReturn(new BanditStatistics(emptyMap(), Optional.empty()));

    WeightUpdate weightUpdate = getGenerator().getWeightUpdate(emptyList());
    assertEquals(0, weightUpdate.getWeightByVariantName().size());
    assertFalse(weightUpdate.getMaybeWinningVariant().isPresent());
    assertEquals(0, weightUpdate.getPerformancesByVariantName().keySet().size());
  }

  @Test
  public void getWeightUpdate() {
    ObservedArmPerformance armPerformanceA = new ObservedArmPerformance(1, 10, 10);
    ObservedArmPerformance armPerformanceB = new ObservedArmPerformance(2, 9, 11);
    ObservedArmPerformance armPerformanceC = new ObservedArmPerformance(3, 8, 12);
    List<ObservedArmPerformance> armPerformances = ImmutableList.of(armPerformanceA, armPerformanceB, armPerformanceC);

    when(bandit.getBanditStatistics(new BanditPerformance(armPerformances)))
        .thenReturn(new BanditStatistics(ImmutableMap.of(1, 0.45, 2, 0.35, 3, 0.20), Optional.empty()));

    WeightUpdate weightUpdate = getGenerator().getWeightUpdate(armPerformances);
    assertEquals(ImmutableMap.of(1, 0.45, 2, 0.35, 3, 0.20), weightUpdate.getWeightByVariantName());
    assertFalse(weightUpdate.getMaybeWinningVariant().isPresent());
    assertEquals(ImmutableMap.of(1, armPerformanceA, 2, armPerformanceB, 3, armPerformanceC),
        weightUpdate.getPerformancesByVariantName());
  }

  @Test
  public void getWeightUpdate_withWinningVariant() {
    ObservedArmPerformance armPerformanceA = new ObservedArmPerformance(1, 10, 5);
    ObservedArmPerformance armPerformanceB = new ObservedArmPerformance(2, 6, 9);
    ObservedArmPerformance armPerformanceC = new ObservedArmPerformance(3, 4, 11);
    List<ObservedArmPerformance> armPerformances = ImmutableList.of(armPerformanceA, armPerformanceB, armPerformanceC);

    when(bandit.getBanditStatistics(new BanditPerformance(armPerformances)))
        .thenReturn(new BanditStatistics(ImmutableMap.of(1, 0.9, 2, 0.09, 3, 0.01), Optional.of(1)));

    WeightUpdate weightUpdate = getGenerator().getWeightUpdate(armPerformances);
    assertEquals(ImmutableMap.of(1, 0.9, 2, 0.09, 3, 0.01), weightUpdate.getWeightByVariantName());
    assertEquals(1, weightUpdate.getMaybeWinningVariant().get().intValue());
    assertEquals(ImmutableMap.of(1, armPerformanceA, 2, armPerformanceB, 3, armPerformanceC),
        weightUpdate.getPerformancesByVariantName());
  }

  private WeightGeneratorImpl getGenerator() {
    WeightGeneratorImpl generator = new WeightGeneratorImpl();
    generator.bandit = bandit;
    return generator;
  }
  
}
