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

/** Google maps's markers's class. */
public class Marker {
  private double lat;
  private double lng;

  private long visitHour;
  private long visitMinute;

  private long leaveHour;
  private long leaveMinute;

  private String markerName;

  public Marker(
      double newLat,
      double newLng,
      long newVisitHour,
      long newVisitMinute,
      long newLeaveHour,
      long newLeaveMinute,
      String newMarkerName) {
    lat = newLat;
    lng = newLng;

    visitHour = newVisitHour;
    visitMinute = newVisitMinute;

    leaveHour = newLeaveHour;
    leaveMinute = newLeaveMinute;

    markerName = newMarkerName;
  }

  public Marker() {
    lat = 0.0;
    lng = 0.0;

    visitHour = 0;
    visitMinute = 0;

    leaveHour = 0;
    leaveMinute = 0;

    markerName = "Place 0";
  }

  public double getLat() {
    return lat;
  }

  public double getLng() {
    return lng;
  }

  public long getVisitHour() {
    return visitHour;
  }

  public long getVisitMinute() {
    return visitMinute;
  }

  public long getLeaveHour() {
    return leaveHour;
  }

  public long getLeaveMinute() {
    return leaveMinute;
  }

  public String getMarkerName() {
    return markerName;
  }

  public void setLat(double newLat) {
    lat = newLat;
  }

  public void setLng(double newLng) {
    lng = newLng;
  }

  public void setVisitHour(long newHour) {
    visitHour = newHour;
  }

  public void setVisitMinute(long newMinute) {
    visitMinute = newMinute;
  }

  public void setLeaveHour(long newHour) {
    leaveHour = newHour;
  }

  public void setLeaveMinute(long newMinute) {
    leaveMinute = newMinute;
  }

  public void setMarkerName(String newName) {
    markerName = newName;
  }
}
