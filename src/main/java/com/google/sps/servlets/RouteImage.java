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
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.data.Images;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@SuppressWarnings("serial")
public class RouteImage extends HttpServlet {
  /** Store route's image. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      String routeId = request.getParameter("route-id");
      Boolean userAccess = true;

      UserService userService = UserServiceFactory.getUserService();
      String userId = userService.getCurrentUser().getUserId();
      Key userKey = KeyFactory.createKey("User", userId);

      // Check if user has access to change the image.
      Filter routeIdFilter =
          new FilterPredicate("routeId", FilterOperator.EQUAL, Long.parseLong(routeId));
      Query routeLink = new Query("RouteUserLink").setAncestor(userKey);
      routeLink.setFilter(routeIdFilter);

      List<Entity> routeUserLink =
          datastore.prepare(routeLink).asList(FetchOptions.Builder.withDefaults());

      if (routeUserLink.isEmpty()) {
        userAccess = false;
      }

      if (userAccess) {
        Key routeKey = KeyFactory.createKey("Route", Long.parseLong(routeId));
        Entity routeEntity = datastore.get(routeKey);
        String fileName = (String) routeEntity.getProperty("imageName");
        if (fileName.equals("globe.jpg")) {
          fileName = routeId + ".png";
          routeEntity.setProperty("imageName", fileName);
          datastore.put(routeEntity);
        }

        Part filePart = request.getPart("route-image");

        // Get the InputStream to store the file until it processed.
        InputStream fileInputStream = filePart.getInputStream();

        Images.uploadObject(
            "route-image-globes", "theglobetrotter-step-2020", fileName, fileInputStream);
      }
    } catch (Exception e) {
      // TODO(#14): Catch more specific exceptions.
    }

    response.sendRedirect("/profile.html");
  }
}
