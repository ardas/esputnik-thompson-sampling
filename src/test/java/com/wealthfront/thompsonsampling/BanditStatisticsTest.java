package com.wealthfront.thompsonsampling;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class BanditStatisticsTest {

  @Test
  public void getters() {
    BanditStatistics banditStatistics = new BanditStatistics(
        ImmutableMap.of(1, 0.8, 2, 0.2), Optional.of(1));
    assertEquals(ImmutableMap.of(1, 0.8, 2, 0.2), banditStatistics.getWeightsByVariant());
    assertEquals(1, banditStatistics.getVictoriousVariant().get().intValue());
  }

}
