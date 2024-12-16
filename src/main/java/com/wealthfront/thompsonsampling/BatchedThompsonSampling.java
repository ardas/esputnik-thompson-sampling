package com.wealthfront.thompsonsampling;

import cern.jet.random.Beta;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;


public class BatchedThompsonSampling implements BatchedBandit {

    private static final int NUMBER_OF_DRAWS = 10_000;
    private static final double CONFIDENCE_LEVEL = 0.95;
    private static final double EXPERIMENT_VALUE_QUIT_LEVEL = 0.01;


    @Override
    public BanditStatistics getBanditStatistics(BanditPerformance performance) {
        List<ObservedArmPerformance> performances = performance.getPerformances();
        int n = performances.size();
        double[][] table = new double[getNumberOfDraws()][n];
        int[] wins = new int[n];
//    List<Beta> probabilityDensityFunctions = getProbabilityDensityFunctions(performances);

        fillProbabilities(performances, table);

        for (int i = 0; i < getNumberOfDraws(); i++) {
            double maxValue = -1.0;
            int winningArm = -1;
            for (int j = 0; j < n; j++) {
//        Beta pdf = probabilityDensityFunctions.get(j);
//        table[i][j] = pdf.nextDouble();
                if (table[i][j] > maxValue) {
                    maxValue = table[i][j];
                    winningArm = j;
                }
            }
            wins[winningArm] += 1;
        }


        System.out.println("############################");
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < getNumberOfDraws(); i++) {
                System.out.print(table[i][j] + " ");
            }
            System.out.println();
        }



        Map<Integer, Double> armWeightsByVariant = new HashMap<>();
        int bestArm = -1;
        double bestWeight = -1.0;
        for (int i = 0; i < n; i++) {
            double weight = (1.0 * wins[i]) / getNumberOfDraws();
            if (weight > bestWeight) {
                bestWeight = weight;
                bestArm = i;
            }
            armWeightsByVariant.put(performances.get(i).getVariantId(), weight);
        }
        if (bestWeight > getConfidenceLevel()) {
            return new BanditStatistics(
                armWeightsByVariant, Optional.of(performances.get(bestArm).getVariantId()));
        }

        double[] valueRemaining = new double[getNumberOfDraws()];
        for (int i = 0; i < getNumberOfDraws(); i++) {
            double maxValue = -1.0;
            int winningArm = -1;
            for (int j = 0; j < n; j++) {
                if (table[i][j] > maxValue) {
                    maxValue = table[i][j];
                    winningArm = j;
                }
            }
            if (winningArm == bestArm) {
                valueRemaining[i] = 0.0;
            } else {
                valueRemaining[i] = (maxValue - table[i][bestArm]) / table[i][bestArm];
            }
        }

        Percentile percentile = new Percentile();
        percentile.setData(valueRemaining);
        double likelyValueRemaining = percentile.evaluate(getConfidenceLevel() * 100.0);
        Optional<Integer> victoriousVariant = likelyValueRemaining < getExperimentValueQuitLevel() ?
            Optional.of(performances.get(bestArm).getVariantId()) : Optional.empty();
        return new BanditStatistics(armWeightsByVariant, victoriousVariant);
    }

    private void fillProbabilities(List<ObservedArmPerformance> performances, double[][] table) {

        final RandomEngine randomEngine = getRandomEngine();
        for (int i = 0; i < performances.size(); i++) {
            final ObservedArmPerformance observedArmPerformance = performances.get(i);
            final double[] probabilities = getCachedProbabilities(observedArmPerformance, getNumberOfDraws(), randomEngine);
            for (int j = 0; j < getNumberOfDraws(); j++) {
                table[j][i] = probabilities[j];
            }
        }
    }

    @Deprecated
    protected List<Beta> getProbabilityDensityFunctions(List<ObservedArmPerformance> performances) {
        return performances.stream().map(armPerformance -> {
            double alpha = armPerformance.getSuccesses() + 1;
            double beta = armPerformance.getFailures() + 1;
            return new Beta(alpha, beta, getRandomEngine());
        }).collect(toList());
    }

    protected Beta getProbabilityDensityFunction(ObservedArmPerformance armPerformance, RandomEngine randomEngine) {
        double alpha = armPerformance.getSuccesses() + 1;
        double beta = armPerformance.getFailures() + 1;
        return new Beta(alpha, beta, randomEngine);
    }

    private final Cache<ObservedArmPerformance, double[]> cachedProbabilities = probabilityCacheBuilder();

    protected Cache<ObservedArmPerformance, double[]> probabilityCacheBuilder() {
        return CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();
    }

    @SneakyThrows
    protected double[] getCachedProbabilities(ObservedArmPerformance performance,
                                              int steps,
                                              RandomEngine randomEngine) {
        return cachedProbabilities.get(performance, () -> {
            final Beta pdf = getProbabilityDensityFunction(performance, randomEngine);

            double[] probabilities = new double[steps];
            for (int i = 0; i < steps; i++) {
                probabilities[i] = pdf.nextDouble();
            }

            return probabilities;
        });
    }


    @VisibleForTesting
    public RandomEngine getRandomEngine() {
        return new MersenneTwister(new Date());
    }

    protected int getNumberOfDraws() {
        return NUMBER_OF_DRAWS;
    }

    protected double getConfidenceLevel() {
        return CONFIDENCE_LEVEL;
    }

    protected double getExperimentValueQuitLevel() {
        return EXPERIMENT_VALUE_QUIT_LEVEL;
    }

}
