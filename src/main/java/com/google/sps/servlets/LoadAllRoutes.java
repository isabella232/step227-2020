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
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.gson.Gson;
import com.google.sps.data.Marker;
import com.google.sps.data.Route;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Responsible for listing comments. */
@SuppressWarnings("serial")
@WebServlet("/show-suggestions")
public class LoadAllRoutes extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Filter publicityFilter = new FilterPredicate("isPublic", FilterOperator.EQUAL, true);
    Query routesQuery = new Query("Route").setFilter(publicityFilter);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery queriedRoutes = datastore.prepare(routesQuery);

    List<Route> routes = new ArrayList<Route>();

    // Create Route instances from entities and add them to the list.
    for (Entity routeEntity : queriedRoutes.asIterable()) {
      String routeName = (String) routeEntity.getProperty("routeName");
      boolean isPublic = (Boolean) routeEntity.getProperty("isPublic");
      boolean isCompleted = (Boolean) routeEntity.getProperty("isCompleted");
      long startHour = (Long) routeEntity.getProperty("startHour");
      long startMinute = (Long) routeEntity.getProperty("startMinute");
      long numberOfRatings = (Long) routeEntity.getProperty("numberOfRatings");
      double sumOfRatings = (double) routeEntity.getProperty("sumOfRatings");
      String routeImage = (String) routeEntity.getProperty("imageName");

      long routeId = routeEntity.getKey().getId();
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
      route.setImage(routeImage);

      Query markersQuery = new Query("Marker").setAncestor(routeEntity.getKey());
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
      routes.add(route);
    }

    Gson gson = new Gson();

    // Respond with the resulted list.
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(routes));
  }
}
