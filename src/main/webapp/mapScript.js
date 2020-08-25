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

let map;
var globalIndex = 0;
var markersArray = [];
var listener;
var editorsArray = [];
var viewersArray = [];

function initInactiveMap() {
  // Create a map object, and include the MapTypeId to add
  // to the map type control.
  map = new google.maps.Map(document.getElementById("map"), {
    center: new google.maps.LatLng(47.0, 5.15),
    zoom: 2.8,
    fullscreenControl: false,
    streetViewControl: false,
    mapTypeControlOptions: {
      mapTypeIds: ["roadmap", "satellite", "hybrid", "terrain"],
    },
  });
}

function initMap() {
  initInactiveMap();

  checkLog().then((loggedIn) => {
    if (loggedIn == true) {
      listener = map.addListener("click", function (event) {
        placeMarker(event.latLng);
      });

      console.log("Initialise new map");

      async function placeMarker(location) {
        let marker = new google.maps.Marker({
          position: location,
          map: map,
        });

        let index = globalIndex,
          stayHour = 0,
          stayMinute = 0,
          markerName = "Press the settings button to choose a name";

        markersArray.push({
          marker: marker,
          data: {
            index: index,
            lat: location.lat(),
            lng: location.lng(),
            stayHour: stayHour,
            stayMinute: stayMinute,
            markerName: markerName,
          },
        });
        addNewTableItem(markerName, index.toString());
        globalIndex = globalIndex + 1;
      }
    }
  });
}

function addNewTableItem(name, index) {
  let tabel = document.getElementById("places-table");

  let newPlace = document.createElement("li");
  newPlace.id = "place" + index;
  newPlace.classList.add("new-place");

  let markerSign = '<span class="fas fa-ellipsis-v"></span>';
  let placeName = '<span id="place-name">' + name + "</span>";
  let deleteFunction = "deletePlace('" + index + "')";
  let showSelectFunction = "showSettings('" + index + "')";
  let settingsButton =
    '<span class="fas fa-cog" onclick="' + showSelectFunction + '"></span>';
  let deleteSign =
    '<span class="fas fa-minus-square" onclick="' +
    deleteFunction +
    '"></span>';

  newPlace.innerHTML = markerSign + placeName + deleteSign + settingsButton;

  tabel.appendChild(newPlace);
}

function getActualIndex(placeIndex) {
  var actualIndex = 0;
  for (var i = 0; i < markersArray.length; i++) {
    if (markersArray[i].data.index == placeIndex) {
      actualIndex = i;
      break;
    }
  }
  return actualIndex;
}

function deletePlace(placeIndex) {
  let tableItem = document.getElementById("place" + placeIndex);
  tableItem.parentNode.removeChild(tableItem);

  var actualIndex = getActualIndex(placeIndex);
  markersArray[actualIndex].marker.setMap(null);
  markersArray.splice(actualIndex, 1);
}

function showSettings(placeIndex) {
  var actualIndex = getActualIndex(placeIndex);
  var settings = document.getElementsByClassName("marker-setting")[0];
  document.getElementById("submit-button").onclick = function () {
    updateMarkerSettings(placeIndex);
  };

  // Show popup.
  // setting.style.visibility = "visible";
  settings.classList.toggle("show");
  document.getElementById("marker-name").value =
    markersArray[actualIndex].data.markerName;
  document.getElementById("stay-hour").value =
    markersArray[actualIndex].data.stayHour;
  document.getElementById("stay-minute").value =
    markersArray[actualIndex].data.stayMinute;
}

function updateMarkerSettings(placeIndex) {
  let markerName = document.getElementById("marker-name").value,
    stayHour = document.getElementById("stay-hour").value,
    stayMinute = document.getElementById("stay-minute").value;

  var actualIndex = getActualIndex(placeIndex);

  markersArray[actualIndex].data.markerName = markerName;
  markersArray[actualIndex].data.stayHour = stayHour;
  markersArray[actualIndex].data.stayMinute = stayMinute;

  let tableItemElemens = document.getElementById("place" + placeIndex)
    .childNodes;
  tableItemElemens[1].innerHTML = markerName;
}

async function createRoute() {
  let routeName = document.getElementById("route-name").value,
    routeId = 0,
    isPublic = Boolean(document.getElementById("publicity").value == 1),
    startHour = document.getElementById("start-hour").value,
    startMinute = document.getElementById("start-minute").value;
  if (routeName == "") {
    alert("Please add a name to your new route!");
  } else {
    var markersData = [];
    for (var i = 0; i < markersArray.length; i++) {
      markersData.push(markersArray[i].data);
    }

    var routeData = {
      routeId: routeId,
      routeName: routeName,
      routeMarkers: markersData,
      isPublic: isPublic,
      startHour: startHour,
      startMinute: startMinute,
      editorsArray: editorsArray,
    };
    console.log(routeData);

    let options = {
      method: "POST",
      body: JSON.stringify(routeData),
      headers: {
        "Content-Type": "application/json",
      },
    };

    await fetch("/storeRoute", options)
      .then((response) => response.json())
      .then((jsonResponse) => {
        if (jsonResponse.hasOwnProperty("errorMessage")) {
          alert(jsonResponse.errorMessage);
        } else {
          if (jsonResponse.isPublic === true) {
            let routesGrid = document.getElementById("routes-grid");
            if (routesGrid.innerHTML === "No suggestions available!") {
              routesGrid.innerHTML = "";
            }
            routesGrid.appendChild(createRouteCard(jsonResponse));
          }
        }
      });
    removeRouteInfo();
    alert(
      "Route successfully created!\nYou can see new created routes on your profile page!"
    );
  }
}

function removeRouteInfo() {
  document.getElementById("places-table").innerHTML = "";
  document.getElementById("route-name").value = "";
  document.getElementById("start-hour").value = -1;
  document.getElementById("start-minute").value = -1;
  privateRoute();
  for (var i = 0; i < markersArray.length; i++) {
    markersArray[i].marker.setMap(null);
  }
  markersArray = [];
  editorsArray = [];
}

function updateShareList() {
  // For now lets assume that Friend Code is ID in datastore.
  // For now lets assume that all co-owners are editors.
  editorsArray.push(document.getElementById("friend-code").value);

  var section = document.getElementById("share-section");
  // If section is visible the next line will make it invisible and vice versa.
  section.classList.toggle("show");
  document.getElementById("friend-code").value = "";
}

function publicRoute() {
  document.getElementById("publicity").value = 1;
  document.getElementsByClassName("fa-users")[0].style.color = "green";
  document.getElementsByClassName("fa-users-slash")[0].style.color = "grey";
}

function privateRoute() {
  document.getElementById("publicity").value = 0;
  document.getElementsByClassName("fa-users")[0].style.color = "grey";
  document.getElementsByClassName("fa-users-slash")[0].style.color = "red";
}
