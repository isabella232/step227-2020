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

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.gson.Gson;
import com.google.sps.data.Marker;
import com.google.sps.data.Result;
import com.google.sps.data.Route;
import java.io.IOException;
import java.util.*;
import java.util.ArrayList;
import java.util.Collections;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet("/getRoute")
public class GetRoute extends HttpServlet {
  @Override
  /** Returns the route coresponding with a specific routeId. */
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Gson gson = new Gson();
    response.setContentType("application/json;");

    // Get routeId from the query string.
    Long routeId = Long.parseLong(request.getParameter("routeId"));
    Key routeKey = KeyFactory.createKey("Route", routeId);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    try {
      Entity routeEntity = datastore.get(routeKey);

      String routeName = (String) routeEntity.getProperty("routeName");
      boolean isPublic = (Boolean) routeEntity.getProperty("isPublic");
      boolean isCompleted = (Boolean) routeEntity.getProperty("isCompleted");
      long startHour = (Long) routeEntity.getProperty("startHour");
      long startMinute = (Long) routeEntity.getProperty("startMinute");
      long numberOfRatings = (Long) routeEntity.getProperty("numberOfRatings");
      double sumOfRatings = (double) routeEntity.getProperty("sumOfRatings");

      Route route =
          new Route(
              routeId,
              routeName,
              isPublic,
              isCompleted,
              startHour,
              startMinute,
              numberOfRatings,
              sumOfRatings);

      Query markersQuery = new Query("Marker").setAncestor(routeKey);
      PreparedQuery associatedMarkers = datastore.prepare(markersQuery);
      List<Marker> markers = new ArrayList<Marker>();

      for (Entity markerEntity : associatedMarkers.asIterable()) {
        Marker marker =
            new Marker(
                (Long) markerEntity.getProperty("index"),
                (double) markerEntity.getProperty("lat"),
                (double) markerEntity.getProperty("lng"),
                (Long) markerEntity.getProperty("stayHour"),
                (Long) markerEntity.getProperty("stayMinute"),
                (String) markerEntity.getProperty("markerName"),
                (double) markerEntity.getProperty("rating"));
        markers.add(marker);
      }

      Collections.sort(
          markers,
          (o1, o2) -> (new Long(o1.getId()).intValue()) - (new Long(o2.getId()).intValue()));
      route.setRouteMarkers(markers);

      Result<Route> routeObject = new Result<Route>(true, "Route successfully retrieved", route);
      response.getWriter().println(gson.toJson(routeObject));

    } catch (EntityNotFoundException e) {
      Result<Route> routeError = new Result<Route>(false, "Error getting Route from DataStore");
      response.getWriter().println(gson.toJson(routeError));
    }
  }
}
