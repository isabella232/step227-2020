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

// TODO(#13): Add owner's id property.
/** Google maps's markers's class. */
public class Marker {
  private double lat;
  private double lng;

  private int stayHour;
  private int stayMinute;

  private String markerName;

  public Marker(
      double newLat, double newLng, int newStayHour, int newStayMinute, String newMarkerName) {
    lat = newLat;
    lng = newLng;

    stayHour = newStayHour;
    stayMinute = newStayMinute;

    markerName = newMarkerName;
  }

  public Marker() {
    lat = 0.0;
    lng = 0.0;

    stayHour = 0;
    stayMinute = 0;

    markerName = "Place 0";
  }

  public double getLat() {
    return lat;
  }

  public double getLng() {
    return lng;
  }

  public int getStayHour() {
    return stayHour;
  }

  public int getStayMinute() {
    return stayMinute;
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

  public void setStayHour(int newHour) {
    stayHour = newHour;
  }

  public void setStayMinute(int newMinute) {
    stayMinute = newMinute;
  }

  public void setMarkerName(String newName) {
    markerName = newName;
  }
}
