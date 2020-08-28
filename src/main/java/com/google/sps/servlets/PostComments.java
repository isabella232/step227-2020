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
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.Result;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Responsible for storing comments. */
@SuppressWarnings("serial")
@WebServlet("/post-comment")
public class PostComments extends HttpServlet {
  class CommentData {
    Long routeId;
    String commentText;

    CommentData(Long routeId, String commentText) {
      this.routeId = routeId;
      this.commentText = commentText;
    }

    Long getRouteId() {
      return routeId;
    }

    String getCommentText() {
      return commentText;
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Gson gson = new Gson();
    response.setContentType("application/json;");

    // Check if user is logged in.
    if (!userService.isUserLoggedIn()) {
      Result userError = new Result("You are not logged in!", false);
      response.getWriter().println(gson.toJson(userError));
      return;
    }

    // Read request body.
    String requestBody =
        request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

    // Convert it to Gson Object.
    CommentData commentData = gson.fromJson(requestBody, CommentData.class);

    // Get the input from the form.
    String comment = commentData.getCommentText();
    Long routeId = commentData.getRouteId();

    Key routeKey = KeyFactory.createKey("Route", routeId);
    Key userKey = KeyFactory.createKey("User", userService.getCurrentUser().getUserId());

    try {
      Entity userEntity = datastore.get(userKey);

      // Store the comments as entities.
      Entity commentEntity = new Entity("Comment", routeKey);
      commentEntity.setProperty("text", comment);
      commentEntity.setProperty("nickname", userEntity.getProperty("nickname"));

      datastore.put(commentEntity);
      Result commentSubmitted = new Result("Comment submitted!", true);
      response.getWriter().println(gson.toJson(commentSubmitted));

    } catch (Exception e) {
      Result userError = new Result("User not found!", false);
      response.getWriter().println(gson.toJson(userError));
    }
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
}
