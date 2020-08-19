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

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;
import com.google.sps.data.Marker;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that process information about user's markers. */
@SuppressWarnings("serial")
@WebServlet("/markers")
public class MapMarker extends HttpServlet {
  /** Processes GET request to return all stored markers. */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Marker");

    // Get access to dataStore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    Gson gson = new Gson();

    // Get list of markers.
    List<Marker> markersList = new ArrayList<>();
    int i = 0;
    for (Entity entity : results.asIterable()) {
      double lat = 0, lng = 0;
      Long id;
      String name;

      int visitHour = 0, visitMinute = 0, leaveHour = 0, leaveMinute = 0;
      i += 1;

      lat = (Double) entity.getProperty("lat");
      lng = (Double) entity.getProperty("lng");
      id = entity.getKey().getId();
      name = (String) entity.getProperty("name");

      Marker marker =
          new Marker(lat, lng, id, visitHour, visitMinute, leaveHour, leaveMinute, name);

      markersList.add(marker);
    }

    // Convert list of markers to JSON format.
    String json = gson.toJson(markersList);

    // Return response to the request.
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  /** Processes POST request by storing received markers. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Read request body.
    String requestBody =
        request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

    // Convert it to Marker object.
    Gson gson = new Gson();
    Marker newMarker = gson.fromJson(requestBody, Marker.class);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // To store new marker.
    if (newMarker.getId() == -1) {
      // Create new entity.
      Entity markerEntity = new Entity("Marker");

      markerEntity.setProperty("lat", newMarker.getLat());
      markerEntity.setProperty("lng", newMarker.getLng());

      markerEntity.setProperty("visitHour", newMarker.getVisitHour());
      markerEntity.setProperty("visitMinute", newMarker.getVisitMinute());

      markerEntity.setProperty("leaveHour", newMarker.getLeaveHour());
      markerEntity.setProperty("leaveMinute", newMarker.getLeaveMinute());

      markerEntity.setProperty("name", newMarker.getMarkerName());

      // Store new marker.
      datastore.put(markerEntity);

      Long id = markerEntity.getKey().getId();
      gson = new Gson();
      String json = gson.toJson(id);

      response.setContentType("application/json;");
      response.getWriter().println(json);
      return;
    }

    // To update settings on an existing  markers.
    Long id = newMarker.getId();

    try {
      Entity markerEntity = datastore.get(KeyFactory.createKey("Marker", id));

      markerEntity.setProperty("visitHour", newMarker.getVisitHour());
      markerEntity.setProperty("visitMinute", newMarker.getVisitMinute());

      markerEntity.setProperty("leaveHour", newMarker.getLeaveHour());
      markerEntity.setProperty("leaveMinute", newMarker.getLeaveMinute());

      markerEntity.setProperty("name", newMarker.getMarkerName());
      datastore.put(markerEntity);
    } catch(Exception e) {

    }
  }
}
