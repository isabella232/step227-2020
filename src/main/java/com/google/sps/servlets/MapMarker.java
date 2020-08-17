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
import org.json.JSONObject;

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
    for (Entity entity : results.asIterable()) {
      double lat = 0, lng = 0;
      Long id;

      lat = (Double) entity.getProperty("lat");
      lng = (Double) entity.getProperty("lng");
      id = entity.getKey().getId();

      Marker marker = new Marker(lat, lng, id);

      markersList.add(marker);
    }

    // Convert list of comments to JSON format.
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

    // Convert it to JSON.
    JSONObject jsonBody = new JSONObject(requestBody);

    // Create new entity.
    Entity markerEntity = new Entity("Marker");

    markerEntity.setProperty("lat", jsonBody.getDouble("lat"));
    markerEntity.setProperty("lng", jsonBody.getDouble("lng"));

    // Store new marker.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(markerEntity);

    Long id = markerEntity.getKey().getId();
    Gson gson = new Gson();
    String json = gson.toJson(id);

    response.setContentType("application/json;");
    response.getWriter().println(json);
  }
}
