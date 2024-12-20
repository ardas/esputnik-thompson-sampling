package com.wealthfront.thompsonsampling;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class BanditPerformanceTest {

  @Test
  public void getCumulativeRegret_noSamples() {
    BanditPerformance banditPerformance = new BanditPerformance(ImmutableList.of(
        new ObservedArmPerformance(1, 0, 0),
        new ObservedArmPerformance(2, 0, 0)
    ));
    double cumulativeRegret = banditPerformance.getCumulativeRegret();
    assertEquals(0.0, cumulativeRegret, 0.001);
  }

  @Test
  public void getCumulativeRegret_smallSamples() {
    BanditPerformance banditPerformance = new BanditPerformance(ImmutableList.of(
        new ObservedArmPerformance(1, 10, 9),
        new ObservedArmPerformance(2, 8, 8)
    ));
    double cumulativeRegret = banditPerformance.getCumulativeRegret();
    assertEquals(0.421, cumulativeRegret, 0.001);
  }

  @Test
  public void getCumulativeRegret_largeSamples() {
    BanditPerformance banditPerformance = new BanditPerformance(ImmutableList.of(
        new ObservedArmPerformance(1, 100, 90),
        new ObservedArmPerformance(2, 80, 80),
        new ObservedArmPerformance(3, 70, 100)
    ));
    double cumulativeRegret = banditPerformance.getCumulativeRegret();
    assertEquals(23.684, cumulativeRegret, 0.001);
  }

  @Test
  public void getBestArmPerformance_noArms_returnsZero() {
    BanditPerformance banditPerformance = new BanditPerformance(emptyList());
    assertEquals(0.0, banditPerformance.getBestArmPerformance(), 0.001);
  }

  @Test
  public void getBestArmPerformance_singleArm() {
    BanditPerformance banditPerformance = new BanditPerformance(ImmutableList.of(
        new ObservedArmPerformance(0, 1, 1)));
    assertEquals(0.5, banditPerformance.getBestArmPerformance(), 0.001);
  }

  @Test
  public void getBestArmPerformance_multipleArms() {
    BanditPerformance banditPerformance = new BanditPerformance(ImmutableList.of(
        new ObservedArmPerformance(0, 1, 1),
        new ObservedArmPerformance(0, 2, 1)));
    assertEquals(0.666, banditPerformance.getBestArmPerformance(), 0.001);
  }

}
