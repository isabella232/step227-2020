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
  checkMode();
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
      if (routesGrid.innerHTML == "")
        routesGrid.innerHTML = "No suggestions available!";
    });
}

function checkMode() {
  const params = new URLSearchParams(window.location.search);
  if (params.has("mode") && params.has(`routeId`)) {
    let url = `/getRoute?routeId=${params.get("routeId")}`;

    fetch(url)
      .then((response) => response.json())
      .then((result) => {
        if (result.success) {
          if (params.get("mode") == "view") {
            viewRoute(result.object);
          } else {
            editMode(result.object);
          }
        } else {
          alert(result.message);
        }
      });
  }
}

function createRouteCard(route) {
  let routeCard = document.createElement("div");
  routeCard.className = "route-card";

  let routeDetails = document.createElement("div");
  routeDetails.className = "route-details";

  let routeName = document.createElement("p");
  routeName.className = "route-name";
  routeName.innerHTML = route.routeName;

  let viewButton = document.createElement("button");
  viewButton.className = "action-button";
  viewButton.innerHTML = "View route";
  viewButton.onclick = function () {
    viewRoute(route);
  };

  let copyButton = document.createElement("button");
  copyButton.className = "action-button";
  copyButton.innerHTML = "Add to my profile";
  copyButton.onclick = function () {
    addToProfile(route);
  };

  let routeImage = document.createElement("img");
  routeImage.src = "pictures/praga-small.jpg";
  routeImage.alt = "praga";

  routeDetails.appendChild(routeName);
  routeDetails.appendChild(viewButton);
  routeDetails.appendChild(copyButton);

  routeCard.appendChild(routeDetails);
  routeCard.appendChild(routeImage);

  return routeCard;
}

function viewRoute(route) {
  document.getElementById("route-content").style.visibility = "visible";
  document.getElementById("login-required").className = "move-left";
  removeRouteInfo();
  initInactiveMap();

  document.getElementById("create-route-button").style.visibility = "hidden";
  document.getElementById("share-with-friends-button").style.visibility =
    "hidden";

  let routeName = document.getElementById("route-name");
  routeName.value = route.routeName;

  document.getElementById("start-hour").value = route.startHour;
  document.getElementById("start-minute").value = route.startMinute;
  publicRoute();

  // Fill the table and add markers on the map
  let tabel = document.getElementById("places-table");
  for (var i = 0; i < route.routeMarkers.length; i++) {
    let marker = route.routeMarkers[i];
    let newPlace = document.createElement("li");
    newPlace.classList.add("new-place");
    newPlace.innerHTML = marker.markerName;
    tabel.appendChild(newPlace);

    let mapMarker = new google.maps.Marker({
      position: {
        lat: marker.lat,
        lng: marker.lng,
      },
      map: map,
    });

    // Add info window for marker.
    const infowindow = new google.maps.InfoWindow();
    infowindow.setContent(
      "<div><strong>" +
        marker.markerName +
        "</strong><br>" +
        "<span>Rating: </span>" +
        marker.rating +
        "</div>"
    );
    infowindow.open(map, mapMarker);
  }

  let ratingScore = generateRating(route);
  let commentsPanel = createCommentsPanel(route);
  let commentForm = createCommentForm(route);

  // Add button to exit preview mode.
  let backButton = document.createElement("button");
  backButton.classList.add("back-button");
  backButton.innerHTML = "BACK TO ROUTE CREATION";
  backButton.onclick = function () {
    window.open("/index.html", "_self");
  };
  let additionalContent = document.getElementById("additional");
  additionalContent.innerHTML = "";
  additionalContent.appendChild(ratingScore);
  additionalContent.appendChild(commentsPanel);
  additionalContent.appendChild(commentForm);
  additionalContent.appendChild(backButton);
}

async function addToProfile(route) {
  route.status = "COPY";
  let options = {
    method: "POST",
    body: JSON.stringify(route),
    headers: {
      "Content-Type": "application/json",
    },
  };
  await fetch("/storeRoute", options)
    .then((response) => response.json())
    .then((jsonResponse) => {
      if (jsonResponse.hasOwnProperty("message")) {
        alert(jsonResponse.message);
      }
    });
}

function generateRating(route) {
  let ratingElement = document.createElement("p");
  ratingElement.classList.add("route-rating");

  // Check for division by 0 and display according rating.
  if (route.numberOfRatings == 0) {
    ratingElement.innerHTML = "No rating available".bold();
  } else {
    let rating = route.sumOfRatings / route.numberOfRatings;
    ratingElement.innerHTML = `Route rating: ${rating}`.bold();
  }

  return ratingElement;
}

function createCommentForm(route) {
  var commentForm = document.createElement("div");
  var commentArea = document.createElement("textarea");
  commentArea.setAttribute("placeholder", "Leave a comment...");
  commentArea.classList.add("comment-form");

  // Create a submit button
  var submit = document.createElement("button");
  submit.innerHTML = "Submit comment";
  submit.onclick = function () {
    submitComment(route, commentArea.value);
  };

  commentForm.appendChild(commentArea);
  commentForm.appendChild(submit);
  return commentForm;
}

async function submitComment(route, commentText) {
  let commentData = {
    routeId: route.routeId,
    commentText: commentText,
  };
  let options = {
    method: "POST",
    body: JSON.stringify(commentData),
    headers: {
      "Content-Type": "application/json",
    },
  };
  await fetch("/comments", options)
    .then((response) => response.json())
    .then((jsonResponse) => {
      if (jsonResponse.success) {
        let commentsPanel = document.getElementById("comments-panel");
        let comment = createCommentElement(jsonResponse.object);
        commentsPanel.appendChild(comment);
      }
      alert(jsonResponse.message);
    });
}

function createCommentsPanel(route) {
  var commentsPanel = document.createElement("div");
  commentsPanel.id = "comments-panel";

  let url = `/comments?routeId=${route.routeId}`;

  fetch(url)
    .then((response) => response.json())
    .then((comments) => {
      comments.forEach((comment) => {
        let commentElement = createCommentElement(comment);
        commentsPanel.appendChild(commentElement);
      });
      if (commentsPanel == "")
        commentsPanel.innerHTML = "No comments available!";
    });

  return commentsPanel;
}

function createCommentElement(comment) {
  let commentElement = document.createElement("div");
  commentElement.className = "comment-element";

  let commentText = document.createElement("p");
  commentText.innerHTML = comment.commentText;

  let signature = document.createElement("p");
  signature.innerHTML = comment.nickname.bold();

  commentElement.appendChild(signature);
  commentElement.appendChild(commentText);

  return commentElement;
}
