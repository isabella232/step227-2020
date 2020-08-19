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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that process information about user's markers. */
@SuppressWarnings("serial")
@WebServlet("/delete_marker")
public class DeleteMarker extends HttpServlet {
  /** Processes POST request by deleting marker by received id. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get Id from the request.
    Boolean successfulDeletion = true;
    try {
      String contentId = request.getParameter("contentId");
      Long id = Long.parseLong(contentId);

      Key markerKey = KeyFactory.createKey("Marker", id);

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.delete(markerKey);
    } catch (Exception e) {
      // TODO(#14): Catch more specific exceptions.
      successfulDeletion = false;
    }

    Gson gson = new Gson();
    String json = gson.toJson(successfulDeletion);

    // Return response to the request.
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }
}
