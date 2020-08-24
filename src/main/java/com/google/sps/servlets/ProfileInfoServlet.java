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
import com.google.sps.data.User;
import java.io.IOException;
import java.util.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Responsible for storing user info. */
@SuppressWarnings("serial")
@WebServlet("/user-info")
public class ProfileInfoServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    // Check if user is logged in.
    if (!userService.isUserLoggedIn()) {
      response.setContentType("text/html;");
      response.getWriter().println("<p>ERROR: You are not logged in</p>");
      response.getWriter().println("<a href=\"index.html\">Go to home page</button>");
      return;
    }
    // Get the input from the form.
    String firstName = getParameter(request, "first-name", "Not set");
    String lastName = getParameter(request, "last-name", "Not set");
    String nickname = getParameter(request, "nickname", "Anonym");
    boolean notifications = (!(getParameter(request, "notifications", "unmute")).equals("mute"));

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity userEntity = new Entity("User", userService.getCurrentUser().getUserId());
    userEntity.setProperty("firstName", firstName);
    userEntity.setProperty("lastName", lastName);
    userEntity.setProperty("nickname", nickname);
    userEntity.setProperty("notifications", notifications);

    datastore.put(userEntity);

    // Redirect back to the profile page.
    response.sendRedirect("/profile.html");
  }

  /**
   * Return the request parameter, or the default value if the parameter was not specified by the
   * client.
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }
    return value;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    UserService userService = UserServiceFactory.getUserService();

    User currentUser;
    if (userService.isUserLoggedIn()) {
      String userId = userService.getCurrentUser().getUserId();
      Key userKey = KeyFactory.createKey("User", userId);

      try {
        // Return current user's info.
        Entity userEntity = datastore.get(userKey);
        currentUser =
            new User(
                (String) userEntity.getProperty("firstName"),
                (String) userEntity.getProperty("lastName"),
                (String) userEntity.getProperty("nickname"),
                (boolean) userEntity.getProperty("notifications"));
      } catch (Exception e) {
        currentUser = new User("Not set", "Not set", "Not set", false);
      }
    } else {
      currentUser = new User("Undefined", "Undefined", "Undefined", false);
    }

    Gson gson = new Gson();

    // Respond with the user details.
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(currentUser));
  }
}
