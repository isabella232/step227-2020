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

//** Checks login status and display HTML elements accordingly. */
async function checkLog() {
  const response = await fetch("/login");
  const loginInfo = await response.json();

  // Add correspondent link to the log button.
  const logButton = document.getElementById("log-button");
  logButton.href = loginInfo.actionUrl;

  // User is logged in.
  if (loginInfo.loggedIn === true) {
    document.getElementById("profile").style.visibility = "visible";
    logButton.innerText = "LOGOUT";
    // User is not logged in.
  } else {
    document.getElementById("profile").style.visibility = "hidden";
    logButton.innerText = "LOGIN";
  }
}

function showSettings(contentId) {
  var settings = document.getElementsByClassName("marker-setting")[0];
  document.getElementById("submit-button").onclick = function () {
    updateMarkerSettings(contentId);
  };

  // Show popup.
  // setting.style.visibility = "visible";
  settings.classList.toggle("show");
}

async function updateMarkerSettings(contentId) {
  let markerName = document.getElementById("marker-name").value,
    visitHour = document.getElementById("visit-hour").value,
    visitMinute = document.getElementById("visit-minute").value,
    leaveHour = document.getElementById("leave-hour").value,
    leaveMinute = document.getElementById("leave-minute").value,
    lat = -1, lng = -1, id = contentId;
  
  let data = {
    lat,
    lng,
    id,
    visitHour,
    visitMinute,
    leaveHour,
    leaveMinute,
    markerName,
  };

  let options = {
    method: "POST",
    body: JSON.stringify(data),
    headers: {
      "Content-Type": "application/json",
    },
  };
  await fetch("/markers", options);
  console.log("Update marker settings");
}
