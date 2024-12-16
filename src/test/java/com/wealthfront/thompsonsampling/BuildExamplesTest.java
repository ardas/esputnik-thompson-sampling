package com.wealthfront.thompsonsampling;

import cern.jet.random.Beta;
import cern.jet.random.engine.MersenneTwister;
import com.opencsv.CSVReader;
import org.junit.Ignore;
import org.junit.Test;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;


@Ignore
public class BuildExamplesTest {


    private static List<String> messageFromOnePool = new ArrayList<>();

    static  {
        messageFromOnePool.add("3758891");
        messageFromOnePool.add("3758899");
        messageFromOnePool.add("3758907");
        messageFromOnePool.add("3758914");
        messageFromOnePool.add("3758922");
        messageFromOnePool.add("3758930");
        messageFromOnePool.add("3758937");
        messageFromOnePool.add("3758948");
        messageFromOnePool.add("3758956");
        messageFromOnePool.add("3758964");
    }


    @Test
    public void test() throws Exception  {
        final List<ObservedArmPerformance> armPerformances = readFromFile("/home/smagid/sources/fork/esputnik-thompson-sampling/src/test/resources/workflow_92450_x.csv");

        final ExecutorService executorService = Executors.newFixedThreadPool(36);
        CountDownLatch l = new CountDownLatch(36);
        AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < 36; i++) {
            executorService.execute(() -> {
                IntStream.range(0, 100).forEach(value -> {
                        BanditPerformance banditPerformance = new BanditPerformance(armPerformances);
                        BanditStatistics banditStatistics = new BatchedThompsonSampling() { //(new MersenneTwister(1), 5, 0.75, 0.01)
                            private double ctr_prior = 0.2;
                            private double prior_scale = 50;

                            @Override
                            protected List<Beta> getProbabilityDensityFunctions(List<ObservedArmPerformance> performances) {
                                final MersenneTwister randomEngine = new MersenneTwister(new Date());
                                return performances.stream().map(armPerformance -> {
                                    double alpha = armPerformance.getSuccesses() + ctr_prior * prior_scale + +1;
                                    double beta = armPerformance.getFailures() + (1 - ctr_prior) * prior_scale + 1;
                                    return new Beta(alpha, beta, randomEngine);
                                }).collect(toList());
                            }

                            @Override
                            protected double getConfidenceLevel() {
                                return 0.1;
                            }

                            @Override
                            protected int getNumberOfDraws() {
                                return 10000;
                            }
                        }
                            .getBanditStatistics(banditPerformance);

                        System.out.println(">> " + counter.incrementAndGet() + " " +  banditStatistics.getWeightsByVariant() + "|" + banditStatistics.getVictoriousVariant().orElse(-100));
                    }
                );
                l.countDown();
            });

        }

        l.await();

    }



    private List<ObservedArmPerformance> readFromFile(String filePath) throws Exception {
        final List<String[]> rows = readAllLines(Paths.get( filePath) );
        //["failed", "success", "message_id", "x_date"]

        final Map<String, List<ObservedArmPerformance>> byVariant = rows.stream()
            .skip(1)
            .map(strings -> {
                int failed = Integer.parseInt(strings[0].trim());
                int success = Integer.parseInt(strings[1].trim());
                String messageId = strings[2].trim();
                return new ObservedArmPerformance(Integer.valueOf(messageId), success, failed);
            })
//            .filter(observedArmPerformance -> {
//                return messageFromOnePool.contains(observedArmPerformance.getVariantName());
//            })
            .collect(Collectors.groupingBy(ObservedArmPerformance::getVariantName));

        List<ObservedArmPerformance> res = new ArrayList<>();
        byVariant.forEach((s, observedArmPerformances) -> {

            final Optional<ObservedArmPerformance> reduce = observedArmPerformances.stream().reduce((o1, o2) -> {
                return new ObservedArmPerformance(o1.getVariantId(),
                    o1.getSuccesses() + o2.getSuccesses(),
                    o1.getFailures() + o2.getFailures());
            });
            reduce.ifPresent(res::add);
        });

        return res;
    }


    public List<String[]> readAllLines(Path filePath) throws Exception {
        try (Reader reader = Files.newBufferedReader(filePath)) {
            try (CSVReader csvReader = new CSVReader(reader)) {
                return csvReader.readAll();
            }
        }
    }

}
