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

  document.getElementById(contentName).style.display = "grid";

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
  for (var i = 0; i < popups.length; i++) {
    if (popups[i] == popup) {
      continue;
    }
    if (popups[i].classList.contains("show")) {
      popups[i].classList.remove("show");
    }
  }

  // Show popup.
  popup.classList.toggle("show");
}

function loadUserInfo() {
  addLogoutLink();
  loadRoutes();
  showAvatar();
  console.log("Load user's routes");

  fetch("/user-info")
    .then((response) => response.json())
    .then((currentUser) => {
      document.getElementById("first-name").value = currentUser.firstName;
      document.getElementById("last-name").value = currentUser.lastName;
      document.getElementById("nickname").value = currentUser.nickname;
      document.getElementById(
        "friend-code-code"
      ).innerHTML = currentUser.friendCode.bold();
      if (currentUser.notifications === true) {
        document.getElementById("notifications-on").checked = true;
      } else {
        document.getElementById("notifications-off").checked = true;
      }
    });
}

//** Add correspondent link to the logout button. */
async function addLogoutLink() {
  const response = await fetch("/login");
  const loginInfo = await response.json();

  document.getElementById("logout-link").href = loginInfo.actionUrl;
}

// Load all routes for the logged user.
function loadRoutes() {
  fetch("/user-routes")
    .then((response) => response.json())
    .then((routesList) => {
      console.log(routesList);
      for (i in routesList) {
        addRoute(routesList[i]);
      }
    });
}

function addRoute(newRoute) {
  let card = document.createElement("div");
  let container = document.createElement("div");
  let routeDetails = document.createElement("div");
  let routeImg = document.createElement("img");
  let routeRating = document.createElement("div");

  card.classList.add("card");
  container.classList.add("container");
  routeDetails.classList.add("route-details");
  routeImg.classList.add("route-img");
  routeRating.classList.add("rating");

  routeDetails.innerHTML = newRoute["routeName"];
  let emptyStar = '<span class="far fa-star"></span>';
  let halfStar = '<span class="fas fa-star-half-alt"></span>';
  let fullStar = '<span class="fas fa-star checked"></span>';

  let ratingCopy = 0;
  if (newRoute["numberOfRatings"] != 0) {
    ratingCopy = newRoute["sumOfRatings"] / newRoute["numberOfRatings"];
  }
  let numberOfFullStars = Math.floor(ratingCopy);
  ratingCopy -= numberOfFullStars;

  let numberOfHalfStars = 0;
  if (ratingCopy < 0.2) {
    numberOfHalfStars = 0;
  } else if (ratingCopy <= 0.8) {
    numberOfHalfStars = 1;
  } else {
    numberOfFullStars += 1;
  }

  routeRating.innerHTML =
    fullStar.repeat(numberOfFullStars) +
    halfStar.repeat(numberOfHalfStars) +
    emptyStar.repeat(5 - numberOfFullStars - numberOfHalfStars);

  card.appendChild(container);
  container.appendChild(routeDetails);
  container.appendChild(routeImg);
  container.appendChild(routeRating);

  if (newRoute["userAccess"] != "OWNER") {
    document.getElementById("shared-routes").appendChild(card);
  } else if (newRoute["isCompleted"]) {
    document.getElementById("completed-routes").appendChild(card);
  } else {
    document.getElementById("future-routes").appendChild(card);
  }
}

function showSubmitAvatar() {
  document.getElementById("submit-avatar").classList.toggle("show");
}

function showAvatar() {
  fetch("/profile-image", {
    method: "GET",
  })
    .then((response) => response.json())
    .then((avatarName) => {
      let avatarImage = document.createElement("img");
      avatarImage.src =
        "https://storage.cloud.google.com/user-image-globes/" + avatarName;
      avatarImage.alt = "Profile picture";
      document.getElementById("avatar-image").appendChild(avatarImage);
    });
}
