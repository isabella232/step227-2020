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
  var content = document.getElementById("content");

  // Create window which will hide popup and
  // delete itself if user click anywhere else.
  if (createClosePopup) {
    var closePopup = document.createElement("div");
    closePopup.id = "close-popup";
    closePopup.style.position = "absolute";
    closePopup.style.width = "98%";
    closePopup.style.height = "98%";
    closePopup.style.zIndex = "1";
    closePopup.style.top = "0";
    closePopup.style.left = "0";
    closePopup.style.right = "0";
    closePopup.style.bottom = "0";
    closePopup.style.margin = "auto";
    closePopup.onclick = function () {
      document.getElementById("close-popup").remove();
      showFavPlaceDetails(contentName, false);
      console.log("Remove surface to delete popup");
    };
    console.log("Create surface to delete popup");
    content.insertBefore(closePopup, content.firstChild);
  }

  // Show popup.
  popup.classList.toggle("show");
}
