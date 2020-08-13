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

// Make navbar stick to the top of the page when scrolling.
window.onscroll = function () {
  stickyNavbar();
};
function stickyNavbar() {
  var navbar = document.getElementById("navbar");
  var initialNavBarOffset = 308;

  if (window.pageYOffset >= 308) {
    navbar.classList.add("sticky");
  } else {
    navbar.classList.remove("sticky");
  }
}

//** Checks login status and display HTML elements accordingly. */
async function checkLog() {
  const response = await fetch("/login");
  const loginInfo = await response.json();

  // Add correspondent link to the log button.
  const logButton = document.getElementById("log-button");
  logButton.href = loginInfo.actionURL;

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
