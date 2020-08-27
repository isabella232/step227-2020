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
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.Error;
import com.google.sps.data.Marker;
import com.google.sps.data.Route;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that process information about user's markers. */
@SuppressWarnings("serial")
@WebServlet("/storeRoute")
public class RoutesStoring extends HttpServlet {
  String json;

  /** Processes POST request by storing routes. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Gson gson = new Gson();

    // Check if user is logged in.
    if (!userService.isUserLoggedIn()) {
      Error userError = new Error("Error: User is not logged in!");

      response.setContentType("application/json;");
      response.getWriter().println(gson.toJson(userError));
      return;
    }

    // Read request body.
    String requestBody =
        request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

    // Convert it to Gson Object.
    Route routeObject = gson.fromJson(requestBody, Route.class);
    List<Marker> routeMarkers = routeObject.getRouteMarkers();
    List<Long> editorsArray = routeObject.getEditorsArray();
    String routeName = routeObject.getRouteName();
    boolean isPublic = routeObject.getIsPublic();
    long startHour = routeObject.getStartHour();
    long startMinute = routeObject.getStartMinute();

    Key userKey = KeyFactory.createKey("User", userService.getCurrentUser().getUserId());

    try {
      Entity routeEntity;
      // Check is this user already has this route stored.
      if (routeObject.getStatus().compareTo("copy") == 0) {
        Filter routeFilter =
            new FilterPredicate("routeId", FilterOperator.EQUAL, routeObject.getRouteId());
        Query routeQuery = new Query("RouteUserLink").setAncestor(userKey).setFilter(routeFilter);
        PreparedQuery checkExistance = datastore.prepare(routeQuery);

        if (checkExistance.countEntities(FetchOptions.Builder.withLimit(10)) > 0) {
          Error userError = new Error("This route is already stored in your library!");
          response.setContentType("application/json;");
          response.getWriter().println(gson.toJson(userError));
          return;
        }
      }
      if (routeObject.getStatus().compareTo("edit") == 0) {
        Key routeKey = KeyFactory.createKey("Route", routeObject.getRouteId());
        routeEntity = datastore.get(routeKey);

        Query markersQuery = new Query("Marker").setAncestor(routeKey);
        PreparedQuery associatedMarkers = datastore.prepare(markersQuery);
        for (Entity markerEntity : associatedMarkers.asIterable()) {
          datastore.delete(markerEntity.getKey());
        }
      } else {
        routeEntity = new Entity("Route");
      }

      routeEntity.setProperty("routeName", routeName);
      routeEntity.setProperty("isPublic", isPublic);
      routeEntity.setProperty("startHour", startHour);
      routeEntity.setProperty("startMinute", startMinute);

      datastore.put(routeEntity);

      for (long userId : editorsArray) {
        Entity linkEntity = new Entity("RouteUserLink", KeyFactory.createKey("User", userId));
        linkEntity.setProperty("routeId", routeEntity.getKey().getId());
        linkEntity.setProperty("type", 2);
        // Add entity for editor.
        datastore.put(linkEntity);
      }

      if (!(routeObject.getStatus().compareTo("new") == 0)) {
        Entity linkEntity = new Entity("RouteUserLink", userKey);
        linkEntity.setProperty("routeId", routeEntity.getKey().getId());
        linkEntity.setProperty("type", 1);
        // Add entity for owner.
        datastore.put(linkEntity);
      }

      for (Marker marker : routeMarkers) {
        // Create entities for markers and make them children of the route.
        Entity markerEntity = new Entity("Marker", routeEntity.getKey());

        markerEntity.setProperty("index", marker.getId());
        markerEntity.setProperty("lat", marker.getLat());
        markerEntity.setProperty("lng", marker.getLng());
        markerEntity.setProperty("stayHour", marker.getStayHour());
        markerEntity.setProperty("stayMinute", marker.getStayMinute());
        markerEntity.setProperty("markerName", marker.getMarkerName());

        // Store new marker.
        datastore.put(markerEntity);
      }

      // Respond with new created route id.
      Long routeId = routeEntity.getKey().getId();
      routeObject.setRouteId(routeId);

      response.setContentType("application/json;");
      response.getWriter().println(gson.toJson(routeObject));

      // TODO(#14): Catch more specific exceptions.
    } catch (Exception e) {
      Error userError = new Error("Error getting User from DataStore");

      response.setContentType("application/json;");
      response.getWriter().println(gson.toJson(userError));
    }
  }
}
