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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.repackaged.com.google.common.collect.ImmutableMap;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public final class ProfileInfoTest {
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
    try {
      localUserServices.setUp();
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  /** Check for new user's information. */
  @Test
  public void testNotSetInfo() throws IOException {
    when(response.getWriter()).thenReturn(new PrintWriter(System.out));
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    LoginServlet loginServlet = new LoginServlet();
    loginServlet.doGet(request, response);

    Query userQuery = new Query("User");
    List<Entity> userList = ds.prepare(userQuery).asList(FetchOptions.Builder.withDefaults());

    if (userList.size() > 0) {
      assertEquals("Not set", userList.get(0).getProperty("firstName"));
    }
  }

  /** Check if existing user can change their information. */
  @Test
  public void testSetNewInfo() throws IOException {
    when(response.getWriter()).thenReturn(new PrintWriter(System.out));
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    LoginServlet loginServlet = new LoginServlet();
    loginServlet.doGet(request, response);

    when(request.getParameter("first-name")).thenReturn("Alice");
    ProfileInfoServlet profileInfo = new ProfileInfoServlet();
    profileInfo.doPost(request, response);

    Query userQuery = new Query("User");
    List<Entity> userList = ds.prepare(userQuery).asList(FetchOptions.Builder.withDefaults());

    if (userList.size() > 0) {
      assertEquals("Alice", userList.get(0).getProperty("firstName"));
    }
  }

  /** Check if non-existent user can't change their information. */
  @Test
  public void testNullUser() throws IOException {
    ProfileInfoServlet profileInfo = new ProfileInfoServlet();
    profileInfo.doPost(request, response);

    verify(response).sendError(HttpServletResponse.SC_NOT_FOUND, "Error user not found");
  }
}
