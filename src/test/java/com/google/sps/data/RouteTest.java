// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.data;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class RouteTest {

  private static Route ROUTE_0 = new Route(0L, "Route0", true, 9L, 30L);
  private static final double delta = 0.0001;

  @Test
  public void getNonDecimalRating() {
    ROUTE_0.setNumberOfRatings(5);
    ROUTE_0.setSumOfRatings(20);

    double expectedRating = 4;
    double actualRating = ROUTE_0.getRating();
    Assert.assertEquals(actualRating, expectedRating, delta);
  }

  @Test
  public void getDecimalRating() {
    ROUTE_0.setNumberOfRatings(5);
    ROUTE_0.setSumOfRatings(22);

    double expectedRating = 4.4;
    double actualRating = ROUTE_0.getRating();
    Assert.assertEquals(actualRating, expectedRating, delta);
  }

  @Test
  public void getZeroRating() {
    ROUTE_0.setNumberOfRatings(0);
    ROUTE_0.setSumOfRatings(22);

    double expectedRating = 0.0;
    double actualRating = ROUTE_0.getRating();
    Assert.assertEquals(actualRating, expectedRating, delta);
  }
}
