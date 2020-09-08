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
  private long id;

  private double lat;
  private double lng;

  private long stayHour;
  private long stayMinute;

  private String markerName;
  private double rating;

  public Marker(
      long newId,
      double newLat,
      double newLng,
      long newStayHour,
      long newStayMinute,
      String newMarkerName,
      double newRating) {
    id = newId;

    lat = newLat;
    lng = newLng;

    stayHour = newStayHour;
    stayMinute = newStayMinute;

    markerName = newMarkerName;
    rating = newRating;
  }

  public Marker() {
    lat = 0.0;
    lng = 0.0;

    stayHour = 0;
    stayMinute = 0;

    markerName = "Place 0";
  }

  public long getId() {
    return id;
  }

  public double getLat() {
    return lat;
  }

  public double getLng() {
    return lng;
  }

  public long getStayHour() {
    return stayHour;
  }

  public long getStayMinute() {
    return stayMinute;
  }

  public String getMarkerName() {
    return markerName;
  }

  public double getRating() {
    return rating;
  }

  public void setId(long newId) {
    this.id = newId;
  }

  public void setLat(double newLat) {
    lat = newLat;
  }

  public void setLng(double newLng) {
    lng = newLng;
  }

  public void setStayHour(long newHour) {
    stayHour = newHour;
  }

  public void setStayMinute(long newMinute) {
    stayMinute = newMinute;
  }

  public void setMarkerName(String newName) {
    markerName = newName;
  }

  public void setRating(double newRating) {
    rating = newRating;
  }
}
