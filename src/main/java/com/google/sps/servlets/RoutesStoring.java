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
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;

/** Servlet that process information about user's markers. */
@SuppressWarnings("serial")
@WebServlet("/storeRoute")
public class RoutesStoring extends HttpServlet {
  /** Processes POST request by storing routes. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Read request body.
    String requestBody =
        request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

    // Convert it to JSON.
    Collection<String> tourPoints = new ArrayList<String>();
    JSONArray jsonArray = new JSONArray(requestBody);
    for (int i = 0; i < jsonArray.length(); i++) {
      tourPoints.add(jsonArray.getJSONObject(i).getString("id"));
    }

    // Get route name.
    String routeName = request.getParameter("routeName");

    // Get user email.
    UserService userService = UserServiceFactory.getUserService();
    String email = userService.getCurrentUser().getEmail();

    // Create new entity.
    Entity routeEntity = new Entity("Route");

    routeEntity.setProperty("name", routeName);
    routeEntity.setProperty("user", email);
    routeEntity.setProperty("tourPoints", tourPoints);

    // Store new marker.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(routeEntity);

    response.sendRedirect("index.html");
  }
}
