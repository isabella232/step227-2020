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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.sps.data.Marker;
import com.google.sps.data.Route;
import com.google.sps.data.RouteStatus;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public final class RoutesStoringTest {
  private LocalServiceTestHelper helper;

  @Mock HttpServletRequest request;

  @Mock HttpServletResponse response;

  @Before
  public void setUp() {
    LocalUserServiceTestConfig localUserServices =
        new LocalUserServiceTestConfig().setOAuthUserId("12345678");
    localUserServices.setOAuthEmail("test@example.com");
    localUserServices.setOAuthAuthDomain("test@example.com");
    LocalDatastoreServiceTestConfig localDatastore = new LocalDatastoreServiceTestConfig();
    helper = new LocalServiceTestHelper(localDatastore, localUserServices);

    helper
        .setEnvIsLoggedIn(true)
        // This envAttributes thing is the only way to set userId.
        // see https://code.google.com/p/googleappengine/issues/detail?id=3579
        .setEnvAttributes(
            ImmutableMap.of("com.google.appengine.api.users.UserService.user_id_key", "12345678"))
        .setEnvAuthDomain("example.com")
        .setEnvEmail("test@example.com")
        .setEnvIsAdmin(true);

    helper.setUp();
    localUserServices.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  /** Check if link between route and user is created. */
  @Test
  public void testRouteLinkIsCreated() throws IOException {
    when(response.getWriter()).thenReturn(new PrintWriter(System.out));

    List<String> editorsArrayTemp = new ArrayList<>();
    List<Marker> routeMarkers = new ArrayList<>();
    RouteStatus status = RouteStatus.NEW;
    Route temp =
        new Route(
            111111, "routeName", true, true, 0, 0, routeMarkers, editorsArrayTemp, 1, 1.0, status);
    Gson gson = new Gson();
    String json = gson.toJson(temp);
    Reader inputString = new StringReader(json);
    BufferedReader reader = new BufferedReader(inputString);
    when(request.getReader()).thenReturn(reader);
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    RoutesStoring routesStoring = new RoutesStoring();
    routesStoring.doPost(request, response);

    Query query = new Query("RouteUserLink");
    List<Entity> entityList = ds.prepare(query).asList(FetchOptions.Builder.withDefaults());

    assertEquals(1, entityList.size());
  }
}
