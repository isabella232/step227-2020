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
import com.google.sps.data.UserImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@SuppressWarnings("serial")
public class ProfileImage extends HttpServlet {
  /** Store user's image. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      UserService userService = UserServiceFactory.getUserService();
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

      String userId = userService.getCurrentUser().getUserId();
      Key userKey = KeyFactory.createKey("User", userId);
      Entity userEntity = datastore.get(userKey);
      String fileName = (String) userEntity.getProperty("avatarName");

      Part filePart = request.getPart("avatar");

      // Get the InputStream to store the file until it processed.
      InputStream fileInputStream = filePart.getInputStream();

      UserImage.uploadObject("theglobetrotter-step-2020", fileName, fileInputStream);
    } catch (Exception e) {
      // TODO(#14): Catch more specific exceptions.
    }

    response.sendRedirect("/profile.html");
  }

  /** Return response with the name for user's profile picture. */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String fileName = "default.png";
    try {
      UserService userService = UserServiceFactory.getUserService();
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

      String userId = userService.getCurrentUser().getUserId();
      Key userKey = KeyFactory.createKey("User", userId);
      Entity userEntity = datastore.get(userKey);
      fileName = (String) userEntity.getProperty("avatarName");
    } catch (Exception e) {
      // TODO(#14): Catch more specific exceptions.
    }

    Gson gson = new Gson();

    // Respond with the user details.
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(fileName));
  }
}
