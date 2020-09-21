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

package com.google.sps.data;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class UserAccessTypeTest {

  private static final UserAccessType OWNER = UserAccessType.OWNER;
  private static final UserAccessType EDITOR = UserAccessType.EDITOR;
  private static final UserAccessType VIEWER = UserAccessType.VIEWER;
  private static final UserAccessType NOT_SET = UserAccessType.NOT_SET;

  @Test
  public void getOwnerType() {
    UserAccessType actual = UserAccessType.getFromValue(1);
    Assert.assertEquals(actual, OWNER);
  }

  @Test
  public void getEditorType() {
    UserAccessType actual = UserAccessType.getFromValue(2);
    Assert.assertEquals(actual, EDITOR);
  }

  @Test
  public void getViewerType() {
    UserAccessType actual = UserAccessType.getFromValue(3);
    Assert.assertEquals(actual, VIEWER);
  }

  @Test
  public void getNotSetType() {
    UserAccessType actual = UserAccessType.getFromValue(100);
    Assert.assertEquals(actual, NOT_SET);
  }
}
