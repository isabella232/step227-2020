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

/**
 * Switch content on the page.
 */
function openContent(contentName) {
  // Update requested content.
  var contentItems = document.getElementsByClassName("content-item");
  for (var i = 0; i < contentItems.length; i++) {
    contentItems[i].style.display = "none";
  }

  document.getElementById(contentName).style.display = "block";

  // Update button desing.
  var barItems = document.getElementsByClassName("bar-item");
  for (var i = 0; i < barItems.length; i++) {
    barItems[i].style.color = "grey";
  }

  var barItem = contentName + "-item";
  document.getElementById(barItem).style.color = "black";
}

/**
 * Show popup with details for chosen place.
 */
function showFavPlaceDetails(contentName, createClosePopup = true) {
  var contentId = contentName + "-popup";
  var popup = document.getElementById(contentId);
  
  var popups = document.getElementsByClassName("popups");
    console.log(popups);
  for (var i = 0; i < popups.length; i++) {
    if (popups[i] == popup) {
      continue;
    }
    popups[i].style.visibility = "hidden";
  }

  // Show popup.
  popup.style.visibility = "visible";
  popup.classList.toggle("show");
}
