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
  loadRoutes();
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

// Show an area to share a route with friends.
function showShareSection() {
  var section = document.getElementById("share-section");
  section.classList.toggle("show");
}

/** Fetches routes from the server and adds them to the suggestions section. */
function loadRoutes() {
  let routesGrid = document.getElementById("routes-grid");
  routesGrid.innerHTML = "";
  fetch("/show-suggestions")
    .then((response) => response.json())
    .then((routes) => {
      routes.forEach((route) => {
        routesGrid.appendChild(createRouteCard(route));
      });
      if (routesGrid === "") routesGrid.innerHTML = "No suggestions available!";
    });
}

function createRouteCard(route) {
  let routeCard = document.createElement("div");
  routeCard.className = "route-card";

  let routeDetails = document.createElement("div");
  routeDetails.className = "route-details";

  let routeName = '<p class="route-name">' + route.routeName + "</p>";
  let button1 =
    '<button class="action-button"><span>See on the map </span></button><br />';
  let button2 =
    '<button class="action-button"><span>Add to future routes </span></button>';

  let routeImage = document.createElement("img");
  routeImage.src = "pictures/praga-small.jpg";
  routeImage.alt = "praga";

  routeDetails.innerHTML = routeName + button1 + button2;
  routeCard.appendChild(routeDetails);
  routeCard.appendChild(routeImage);

  return routeCard;
}
