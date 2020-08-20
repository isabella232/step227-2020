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

function loadPage() {
  checkLog();
}

//** Checks login status and display HTML elements accordingly. */
async function checkLog() {
  var loggedIn;
  const response = await fetch("/login");
  const loginInfo = await response.json();

  // Add correspondent link to the log button.
  const logButton = document.getElementById("log-button");
  logButton.href = loginInfo.actionUrl;

  // User is logged in.
  if (loginInfo.loggedIn === true) {
    document.getElementById("profile").style.visibility = "visible";
    logButton.innerText = "LOGOUT";
    document.getElementById("route-content").style.visibility = "visible";
    loggedIn = true;
    // User is not logged in.
  } else {
    document.getElementById("profile").style.visibility = "hidden";
    logButton.innerText = "LOGIN";
    document.getElementById("login-required").style.visibility = "visible";
    loggedIn = false;
  }
  return loggedIn;
}

// Show a marker's settings area.
function showSettings(contentId) {
  var settings = document.getElementsByClassName("marker-setting")[0];
  document.getElementById("submit-button").onclick = function () {
    updateMarkerSettings(contentId);
  };

  // Show popup.
  // setting.style.visibility = "visible";
  settings.classList.toggle("show");
}

// Show an area to share a route with friends.
function showShareSection() {
  var section = document.getElementById("share-section");
  section.classList.toggle("show");
}
