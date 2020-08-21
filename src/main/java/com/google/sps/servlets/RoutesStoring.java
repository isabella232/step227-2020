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
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.Marker;
import java.io.IOException;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that process information about user's markers. */
@SuppressWarnings("serial")
@WebServlet("/storeRoute")
public class RoutesStoring extends HttpServlet {
  static class RouteData {
    String routeName;
    Marker[] markersData;
    boolean publicity;
    int hour;
    int minute;

    RouteData(String routeName, Marker[] markersData, boolean publicity, int hour, int minute) {
      this.routeName = routeName;
      this.markersData = markersData;
      this.publicity = publicity;
      this.hour = hour;
      this.minute = minute;
    }

    String getRouteName() {
      return routeName;
    }

    Marker[] getMarkersData() {
      return markersData;
    }

    boolean getPublicity() {
      return publicity;
    }

    int getHour() {
      return hour;
    }

    int getMinute() {
      return minute;
    }
  }

  static class Error {
    String errorMessage;

    Error(String errorMessage) {
      this.errorMessage = errorMessage;
    }

    String getErrorMessage() {
      return errorMessage;
    }
  }

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
    RouteData gsonObject = gson.fromJson(requestBody, RouteData.class);
    Marker[] routeMarkers = gsonObject.getMarkersData();
    String routeName = gsonObject.getRouteName();
    boolean publicity = gsonObject.getPublicity();
    int hour = gsonObject.getHour();
    int minute = gsonObject.getMinute();

    Key userKey = KeyFactory.createKey("User", userService.getCurrentUser().getUserId());

    try {
      Entity userEntity = datastore.get(userKey);
      // Create new route entity and make it child of the user.
      Entity routeEntity = new Entity("Route", userEntity.getKey());
      routeEntity.setProperty("name", routeName);
      routeEntity.setProperty("publicity", publicity);
      routeEntity.setProperty("startHour", hour);
      routeEntity.setProperty("startMinute", minute);
      datastore.put(routeEntity);

      for (Marker marker : routeMarkers) {
        // Create entities for markers and make them children of the route.
        Entity markerEntity = new Entity("Marker", routeEntity.getKey());

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

      response.setContentType("application/json;");
      response.getWriter().println(gson.toJson(routeId));

      // TODO(#14): Catch more specific exceptions.
    } catch (Exception e) {
      Error userError = new Error("Error getting User from DataStore");

      response.setContentType("application/json;");
      response.getWriter().println(gson.toJson(userError));
    }
  }
}
