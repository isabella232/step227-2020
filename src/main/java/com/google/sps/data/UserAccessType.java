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

public enum UserAccessType {
  NOT_SET(0),
  OWNER(1),
  EDITOR(2),
  VIEWER(3);

  private int numericValue;

  UserAccessType(int newValue) {
    this.numericValue = newValue;
  }

  public int getValue() {
    return numericValue;
  }

  public static UserAccessType getFromValue(int value) {
    switch (value) {
      case 0:
        return NOT_SET;
      case 1:
        return OWNER;
      case 2:
        return EDITOR;
      case 3:
        return VIEWER;
    }
    return NOT_SET;
  }
}
