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
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.Marker;
import com.google.sps.data.Result;
import com.google.sps.data.Route;
import com.google.sps.data.RouteStatus;
import com.google.sps.data.UserAccessType;
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
    response.setContentType("application/json;");

    // Check if user is logged in.
    if (!userService.isUserLoggedIn()) {
      Result<Route> userError = new Result<Route>(false, "Error: User is not logged in!");
      response.getWriter().println(gson.toJson(userError));
      return;
    }

    // Read request body.
    String requestBody =
        request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

    // Convert it to Gson Object.
    Route routeObject = gson.fromJson(requestBody, Route.class);
    List<Marker> routeMarkers = routeObject.getRouteMarkers();
    List<String> editorsArray = routeObject.getEditorsArray();
    String routeName = routeObject.getRouteName();
    boolean isPublic = routeObject.getIsPublic();
    boolean isCompleted = routeObject.getIsCompleted();
    long startHour = routeObject.getStartHour();
    long startMinute = routeObject.getStartMinute();
    long numberOfRatings = routeObject.getNumberOfRatings();
    double sumOfRatings = routeObject.getSumOfRatings();
    RouteStatus routeStatus = routeObject.getStatus();

    Key userKey = KeyFactory.createKey("User", userService.getCurrentUser().getUserId());

    try {
      Entity routeEntity;
      // Check is this user already has this route stored.
      if (routeStatus == RouteStatus.COPY) {
        Filter routeFilter =
            new FilterPredicate("routeId", FilterOperator.EQUAL, routeObject.getRouteId());
        Query routeQuery = new Query("RouteUserLink").setAncestor(userKey).setFilter(routeFilter);
        PreparedQuery checkExistence = datastore.prepare(routeQuery);

        if (checkExistence.countEntities(FetchOptions.Builder.withLimit(10)) > 0) {
          Result<Route> duplicateError =
              new Result<Route>(false, "This route is already stored in your library!");
          response.getWriter().println(gson.toJson(duplicateError));
          return;
        }
      }
      if (routeStatus == RouteStatus.EDIT) {
        Key routeKey = KeyFactory.createKey("Route", routeObject.getRouteId());
        routeEntity = datastore.get(routeKey);

        // TODO(#29): Batch multiple updates together where possible.
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
      routeEntity.setProperty("isCompleted", isCompleted);
      routeEntity.setProperty("startHour", startHour);
      routeEntity.setProperty("startMinute", startMinute);
      routeEntity.setProperty("numberOfRatings", numberOfRatings);
      routeEntity.setProperty("sumOfRatings", sumOfRatings);

      datastore.put(routeEntity);
      if (routeStatus != RouteStatus.COPY) {
        for (String friendCode : editorsArray) {
          // Find the user with the corresponding friend code.
          Filter friendCodeFilter =
              new FilterPredicate("friendCode", FilterOperator.EQUAL, friendCode);
          Query friendQuery = new Query("User").setFilter(friendCodeFilter);
          List<Entity> friend =
              datastore.prepare(friendQuery).asList(FetchOptions.Builder.withDefaults());

          // If the user is found, make him editor.
          if (friend.size() != 0) {
            Entity linkEntity = new Entity("RouteUserLink", friend.get(0).getKey());
            linkEntity.setProperty("routeId", routeEntity.getKey().getId());
            linkEntity.setProperty("userAccess", UserAccessType.EDITOR.getValue());
            // Add entity for editor.
            datastore.put(linkEntity);
          }
        }
      }

      if (routeStatus != RouteStatus.EDIT) {
        Entity linkEntity = new Entity("RouteUserLink", userKey);
        linkEntity.setProperty("routeId", routeEntity.getKey().getId());
        linkEntity.setProperty("userAccess", UserAccessType.OWNER.getValue());
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
        markerEntity.setProperty("rating", marker.getRating());

        // Store new marker.
        datastore.put(markerEntity);
      }

      switch (routeStatus) {
        case NEW:
          {
            // Respond with new created route.
            Long routeId = routeEntity.getKey().getId();
            routeObject.setRouteId(routeId);
            Result<Route> routeCreated =
                new Result<Route>(true, "Route successfully created!", routeObject);
            response.getWriter().println(gson.toJson(routeCreated));
            break;
          }
        case EDIT:
          {
            Result<Route> editStatus = new Result<Route>(false, "Route editted successfully!");
            response.getWriter().println(gson.toJson(editStatus));
            break;
          }
        case COPY:
          {
            Result<Route> copyStatus = new Result<Route>(false, "Route added to your library");
            response.getWriter().println(gson.toJson(copyStatus));
            break;
          }
      }
    } catch (EntityNotFoundException e) {
      Result<Route> userError = new Result<Route>(false, "Error getting User from DataStore");

      response.setContentType("application/json;");
      response.getWriter().println(gson.toJson(userError));
    }
  }
}
