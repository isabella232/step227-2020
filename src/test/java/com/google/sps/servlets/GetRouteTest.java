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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.*;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public final class GetRouteTest {
  private final String ROUTE_ID = "123456";
  private LocalServiceTestHelper helper;

  @Mock HttpServletRequest request;

  @Mock HttpServletResponse response;

  @Before
  public void setUp() {
    LocalDatastoreServiceTestConfig localDatastore = new LocalDatastoreServiceTestConfig();
    helper = new LocalServiceTestHelper(localDatastore);
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  /**
   * Tests that when the routeId is not found, the servlet will send
   * HttpServletResponse.SC_NOT_FOUND
   */
  @Test
  public void testEntityNotFoundException() throws IOException {
    when(request.getParameter("routeId")).thenReturn(ROUTE_ID);

    GetRoute routeRequest = new GetRoute();
    routeRequest.doGet(request, response);

    verify(response)
        .sendError(HttpServletResponse.SC_NOT_FOUND, "Error getting Route from DataStore");
  }
}
