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

public final class User {

  private final String fname;
  private final String lname;
  private final String nickname;
  private final boolean notifications;

  public User(String fname, String lname, String nickname, boolean notifications) {
    this.fname = fname;
    this.lname = lname;
    this.nickname = nickname;
    this.notifications = notifications;
  }

  public String getFname() {
    return fname;
  }

  public String getLname() {
    return lname;
  }

  public String getNickname() {
    return nickname;
  }

  public boolean getNotifications() {
    return notifications;
  }
}
