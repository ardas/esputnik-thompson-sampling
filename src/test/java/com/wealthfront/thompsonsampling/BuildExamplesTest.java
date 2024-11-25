package com.wealthfront.thompsonsampling;

import cern.jet.random.Beta;
import com.opencsv.CSVReader;
import org.junit.Ignore;
import org.junit.Test;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        List<ObservedArmPerformance> armPerformances = readFromFile("/home/smagid/sources/fork/esputnik-thompson-sampling/src/test/resources/workflow_92451_stats_by_3day.csv");

        armPerformances.add(new ObservedArmPerformance("100001", 1, 200));

        BanditPerformance banditPerformance = new BanditPerformance(armPerformances);
        BanditStatistics banditStatistics = new BatchedThompsonSampling() { //(new MersenneTwister(1), 5, 0.75, 0.01)
            private double ctr_prior = 0.2;
            private double prior_scale = 50;

            @Override
            protected List<Beta> getProbabilityDensityFunctions(List<ObservedArmPerformance> performances) {
                return performances.stream().map(armPerformance -> {
                    double alpha = armPerformance.getSuccesses() + ctr_prior*prior_scale + + 1;
                    double beta = armPerformance.getFailures() + (1-ctr_prior)*prior_scale + 1;
                    return new Beta(alpha, beta, getRandomEngine());
                }).collect(toList());
            }

            @Override
            protected double getConfidenceLevel() {
                return 0.5;
            }
        }
        .getBanditStatistics(banditPerformance);

        System.out.println(">> " + banditStatistics.getWeightsByVariant() + "|" + banditStatistics.getVictoriousVariant().orElse("No Win"));



        ///print result
        final Map<String, ObservedArmPerformance> armByVariant = armPerformances.stream()
            .collect(Collectors.toMap(ObservedArmPerformance::getVariantName, Function.identity()));

        banditStatistics.getWeightsByVariant().forEach((variantName, weights) -> {
            final ObservedArmPerformance ormp = armByVariant.get(variantName);
            System.out.println(variantName + ";"+ormp.getSuccesses()+";"+ormp.getFailures()+";"+weights);
        });


//        assertEquals(ImmutableMap.of("a", 1.0, "b", 0.0), banditStatistics.getWeightsByVariant());
//        assertEquals("a", banditStatistics.getVictoriousVariant().get());

    }



    private List<ObservedArmPerformance> readFromFile(String filePath) throws Exception {
        final List<String[]> rows = readAllLines(Paths.get( filePath) );
        //["failed", "success", "message_id", "x_date"]

        final Map<String, List<ObservedArmPerformance>> byVariant = rows.stream()
            .skip(1)
            .map(strings -> {
                int failed = Integer.parseInt(strings[0]);
                int success = Integer.parseInt(strings[1]);
                String messageId = strings[2];
                return new ObservedArmPerformance(messageId, success, failed);
            })
            .filter(observedArmPerformance -> {
                return messageFromOnePool.contains(observedArmPerformance.getVariantName());
            })
            .collect(Collectors.groupingBy(ObservedArmPerformance::getVariantName));

        List<ObservedArmPerformance> res = new ArrayList<>();
        byVariant.forEach((s, observedArmPerformances) -> {

            final Optional<ObservedArmPerformance> reduce = observedArmPerformances.stream().reduce((o1, o2) -> {
                return new ObservedArmPerformance(o1.getVariantName(),
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
