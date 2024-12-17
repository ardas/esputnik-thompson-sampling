package com.wealthfront.thompsonsampling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

public class BatchedThompsonSamplingTest {

  @Test
  public void getBanditStatistics() {
    List<ObservedArmPerformance> armPerformances = ImmutableList.of(
        new ObservedArmPerformance(1, 100, 100),
        new ObservedArmPerformance(2, 99, 101)
    );

    BanditPerformance banditPerformance = new BanditPerformance(armPerformances);
    BanditStatistics banditStatistics = getBandit(new MersenneTwister(1), 10, 0.90, 0.01)
        .getBanditStatistics(banditPerformance);
    assertEquals(ImmutableMap.of(1, 0.7, 2, 0.3), banditStatistics.getWeightsByVariant());
    assertFalse(banditStatistics.getVictoriousVariant().isPresent());
  }

  @Test
  public void getBanditStatistics_withWinner_fromWeights() {
    List<ObservedArmPerformance> armPerformances = ImmutableList.of(
        new ObservedArmPerformance(1, 130, 100),
        new ObservedArmPerformance(2, 90, 120));

    BanditPerformance banditPerformance = new BanditPerformance(armPerformances);
    BanditStatistics banditStatistics = getBandit(new MersenneTwister(1), 5, 0.75, 0.01)
        .getBanditStatistics(banditPerformance);
    assertEquals(ImmutableMap.of(1, 1.0, 2, 0.0), banditStatistics.getWeightsByVariant());
    assertEquals(1, banditStatistics.getVictoriousVariant().get().intValue());
  }

  @Test
  public void getBanditStatistics_withWinner_fromQuitValue() {
    List<ObservedArmPerformance> armPerformances = ImmutableList.of(
        new ObservedArmPerformance(1, 10, 10),
        new ObservedArmPerformance(2, 9, 11));

    BanditPerformance banditPerformance = new BanditPerformance(armPerformances);
    BanditStatistics banditStatistics = getBandit(new MersenneTwister(1), 5, 0.90, 2.0)
        .getBanditStatistics(banditPerformance);
    assertEquals(ImmutableMap.of(1, 0.4, 2, 0.6), banditStatistics.getWeightsByVariant());
    assertEquals(2, banditStatistics.getVictoriousVariant().get().intValue());
  }



  private BatchedThompsonSampling getBandit(
      RandomEngine randomEngine,
      int numberOfDraws,
      double confidenceLevel,
      double experimentValueQuitLevel) {
    return new BatchedThompsonSampling() {
      @Override
      public RandomEngine getRandomEngine() {
        return randomEngine;
      }

      @Override
      public int getNumberOfDraws() {
        return numberOfDraws;
      }

      @Override
      public double getConfidenceLevel() {
        return confidenceLevel;
      }

      @Override
      public double getExperimentValueQuitLevel() {
        return experimentValueQuitLevel;
      }
    };
  }

}
