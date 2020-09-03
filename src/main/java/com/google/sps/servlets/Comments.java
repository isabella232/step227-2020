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
import com.google.sps.data.Comment;
import com.google.sps.data.Result;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet("/comments")
public class Comments extends HttpServlet {
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
  /** Responsible for storing comments. */
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Gson gson = new Gson();
    response.setContentType("application/json;");

    // Check if user is logged in.
    if (!userService.isUserLoggedIn()) {
      Result<Comment> userError = new Result<Comment>(false, "You are not logged in!");
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
      String nickname = (String) userEntity.getProperty("nickname");

      // Store the comments as entities.
      Entity commentEntity = new Entity("Comment", routeKey);
      commentEntity.setProperty("text", comment);
      commentEntity.setProperty("nickname", nickname);

      datastore.put(commentEntity);
      Result<Comment> commentSubmitted =
          new Result<Comment>(true, "Comment submitted", new Comment(comment, nickname));
      response.getWriter().println(gson.toJson(commentSubmitted));

    } catch (EntityNotFoundException e) {
      Result<Comment> userError = new Result<Comment>(false, "User not found!");
      response.getWriter().println(gson.toJson(userError));
    }
  }

  @Override
  /** Returns all the comments coresponding with a specific routeId. */
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Gson gson = new Gson();
    Long routeId = Long.parseLong(request.getParameter("routeId"));

    Key routeKey = KeyFactory.createKey("Route", routeId);
    Query commentsQuery = new Query("Comment").setAncestor(routeKey);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery queriedComments = datastore.prepare(commentsQuery);

    List<Comment> comments = new ArrayList<Comment>();

    for (Entity commentEntity : queriedComments.asIterable()) {
      String commentText = (String) commentEntity.getProperty("text");
      String nickname = (String) commentEntity.getProperty("nickname");

      Comment comment = new Comment(commentText, nickname);
      comments.add(comment);
    }

    // Responds with the resulted list.
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(comments));
  }

  /**
   * Returns the request parameter, or the default value if the parameter was not specified by the
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
