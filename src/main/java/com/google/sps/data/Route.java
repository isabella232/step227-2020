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

import java.util.List;

public final class Route {

  private long routeId;
  private String routeName;
  private boolean isPublic;
  private long startHour;
  private long startMinute;
  private List<Marker> routeMarkers;

  public Route(
      long routeId,
      String routeName,
      boolean isPublic,
      long startHour,
      long startMinute,
      List<Marker> routeMarkers) {
    this.routeId = routeId;
    this.routeName = routeName;
    this.isPublic = isPublic;
    this.startHour = startHour;
    this.startMinute = startMinute;
    this.routeMarkers = routeMarkers;
  }

  public Route(long routeId, String routeName, boolean isPublic, long startHour, long startMinute) {
    this.routeId = routeId;
    this.routeName = routeName;
    this.isPublic = isPublic;
    this.startHour = startHour;
    this.startMinute = startMinute;
  }

  public void setRouteMarkers(List<Marker> routeMarkers) {
    this.routeMarkers = routeMarkers;
  }

  public String getRouteName() {
    return routeName;
  }

  public long getRouteId() {
    return routeId;
  }

  public boolean getIsPublic() {
    return isPublic;
  }

  public long getStartHour() {
    return startHour;
  }

  public long getStartMinute() {
    return startMinute;
  }

  public List<Marker> getRouteMarkers() {
    return routeMarkers;
  }
}
