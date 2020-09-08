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
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.Result;
import com.google.sps.data.Route;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet("/markAsCompleted")
public class MarkAsCompleted extends HttpServlet {
  @Override
  /** Mark the route coresponding with a specific routeId as completed. */
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
    String routeName = routeObject.getRouteName();
    boolean isPublic = routeObject.getIsPublic();
    long startHour = routeObject.getStartHour();
    long startMinute = routeObject.getStartMinute();
    long numberOfRatings = routeObject.getNumberOfRatings();
    double sumOfRatings = routeObject.getSumOfRatings();

    try {
      Key routeKey = KeyFactory.createKey("Route", routeObject.getRouteId());
      Entity routeEntity = datastore.get(routeKey);

      // Update the isCompleted property.
      routeEntity.setProperty("routeName", routeName);
      routeEntity.setProperty("isPublic", isPublic);
      routeEntity.setProperty("isCompleted", true);
      routeEntity.setProperty("startHour", startHour);
      routeEntity.setProperty("startMinute", startMinute);
      routeEntity.setProperty("numberOfRatings", numberOfRatings);
      routeEntity.setProperty("sumOfRatings", sumOfRatings);

      datastore.put(routeEntity);

      routeObject.setIsCompleted(true);
      Result<Route> routeResult =
          new Result<Route>(true, "Route marked as completed!", routeObject);
      response.getWriter().println(gson.toJson(routeResult));

    } catch (EntityNotFoundException e) {
      Result<Route> routeError = new Result<Route>(false, "Error getting Route from DataStore");
      response.getWriter().println(gson.toJson(routeError));
    }
  }
}
