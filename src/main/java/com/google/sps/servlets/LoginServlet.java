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
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// ** Returns the login status of a user */
@SuppressWarnings("serial")
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
  static final int FRIEND_CODE_LENGTH = 18;

  static class LoginStatus {
    boolean loggedIn;
    String actionUrl;

    LoginStatus(boolean loggedIn, String actionUrl) {
      this.loggedIn = loggedIn;
      this.actionUrl = actionUrl;
    }

    boolean getLoggedIn() {
      return loggedIn;
    }

    String getActionUrl() {
      return actionUrl;
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    LoginStatus loginStatus;

    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      String logoutUrl = userService.createLogoutURL("/");
      loginStatus = new LoginStatus(true, logoutUrl);

      // Create user entity if it doesn't already exist.
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      String userId = userService.getCurrentUser().getUserId();
      Key userKey = KeyFactory.createKey("User", userId);
      Filter keyFilter =
          new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, userKey);
      Query userQuery = new Query("User").setFilter(keyFilter);
      PreparedQuery checkForUser = datastore.prepare(userQuery);

      // If user entity doesn't already exist, create one.
      if (checkForUser.countEntities(FetchOptions.Builder.withDefaults()) == 0) {
        Entity userEntity = new Entity("User", userId);
        userEntity.setProperty("firstName", "Not set");
        userEntity.setProperty("lastName", "Not set");
        userEntity.setProperty("nickname", "Anonym");
        userEntity.setProperty("notifications", false);

        String friendCode = generateFriendCode();
        userEntity.setProperty("friendCode", friendCode);

        datastore.put(userEntity);
      }

    } else {
      String loginUrl = userService.createLoginURL("/");
      loginStatus = new LoginStatus(false, loginUrl);
    }

    response.setContentType("application/json;");
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(loginStatus));
  }

  public String generateFriendCode() {
    try {
      String values = "0123456789abcdefghijklmnopqrstuvwxyz";
      SecureRandom secureRandom = SecureRandom.getInstanceStrong();

      // Generate a random string of length 12.
      String randomString =
          secureRandom
              .ints(FRIEND_CODE_LENGTH, 0, values.length())
              .mapToObj(i -> values.charAt(i))
              .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
              .toString();
      return randomString;
      // TODO(#36): Fail the user creation and log an error.
    } catch (NoSuchAlgorithmException e) {
      return "";
    }
  }
}
