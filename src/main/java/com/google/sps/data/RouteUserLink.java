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

/** Google maps's markers's class. */
public class RouteUserLink {
  private Long userId;
  private Long routeId;

  // 1 - owner, 2 - editor, 3 - viewer
  private int type;

  public RouteUserLink(Long newUserId, Long newRouteId, int type) {
    userId = newUserId;
    routeId = newRouteId;
    type = type;
  }

  public RouteUserLink() {
    userId = 0L;
    routeId = 0L;
    type = -1;
  }

  public Long getUserId() {
    return userId;
  }

  public Long getRouteId() {
    return routeId;
  }

  public int getType() {
    return type;
  }

  public void setUserId(Long newId) {
    userId = newId;
  }

  public void setRouteId(Long newId) {
    routeId = newId;
  }

  public void setType(int newType) {
    type = newType;
  }
}
