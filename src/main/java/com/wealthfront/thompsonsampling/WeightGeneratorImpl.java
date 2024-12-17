package com.wealthfront.thompsonsampling;

import com.google.common.annotations.VisibleForTesting;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public class WeightGeneratorImpl implements WeightGenerator {

  @VisibleForTesting
  BatchedBandit bandit = new BatchedThompsonSampling();

  @Override
  public WeightUpdate getWeightUpdate(List<ObservedArmPerformance> performances) {
    BanditPerformance performance = new BanditPerformance(performances);
    BanditStatistics statistics = bandit.getBanditStatistics(performance);
    Map<Integer, Double> weightsByVariant = statistics.getWeightsByVariant();
    Map<Integer, ObservedArmPerformance> performancesByVariant = performances.stream()
        .collect(toMap(ObservedArmPerformance::getVariantId, Function.identity()));
    return new WeightUpdate(weightsByVariant, statistics.getVictoriousVariant(), performancesByVariant);
  }

}
