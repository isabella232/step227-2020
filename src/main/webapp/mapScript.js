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
var markersArray = [];
var listener;

function initMap() {
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

        let index = markersArray.length,
          visitHour = 0,
          visitMinute = 0,
          leaveHour = 0,
          leaveMinute = 0,
          markerName = "Place " + markersArray.length.toString();

        markersArray.push({
          marker: marker,
          data: {
            lat: location.lat(),
            lng: location.lng(),
            visitHour: visitHour,
            visitMinute: visitMinute,
            leaveHour: leaveHour,
            leaveMinute: leaveMinute,
            markerName: markerName,
          },
        });
        addNewTableItem(markerName, index.toString());
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

function deletePlace(contentId) {
  let tableItem = document.getElementById("place" + contentId);
  tableItem.parentNode.removeChild(tableItem);
  console.log("Remove place from the table");

  markersArray[contentId].marker.setMap(null);
  markersArray.splice(contentId, 1);
  console.log("remove marker from the array and the map");
}

function showSettings(contentId) {
  var settings = document.getElementsByClassName("marker-setting")[0];
  document.getElementById("submit-button").onclick = function () {
    updateMarkerSettings(contentId);
  };

  // Show popup.
  // setting.style.visibility = "visible";
  settings.classList.toggle("show");
  document.getElementById("marker-name").value =
    markersArray[contentId].data.markerName;
  document.getElementById("visit-hour").value =
    markersArray[contentId].data.visitHour;
  document.getElementById("visit-minute").value =
    markersArray[contentId].data.visitMinute;
  document.getElementById("leave-hour").value =
    markersArray[contentId].data.leaveHour;
  document.getElementById("leave-minute").value =
    markersArray[contentId].data.leaveMinute;
}

function updateMarkerSettings(contentId) {
  let markerName = document.getElementById("marker-name").value,
    visitHour = document.getElementById("visit-hour").value,
    visitMinute = document.getElementById("visit-minute").value,
    leaveHour = document.getElementById("leave-hour").value,
    leaveMinute = document.getElementById("leave-minute").value;

  markersArray[contentId].data.markerName = markerName;
  markersArray[contentId].data.visitHour = visitHour;
  markersArray[contentId].data.visitMinute = visitMinute;
  markersArray[contentId].data.leaveHour = leaveHour;
  markersArray[contentId].data.leaveMinute = leaveMinute;

  let tableItemElemens = document.getElementById("place" + contentId)
    .childNodes;
  tableItemElemens[1].innerHTML = markerName;
}

async function createRoute() {
  var routeName = document.getElementById("route-name").value;
  if (routeName == "") {
    alert("Please add a name to your new route!");
  } else {
    var markersData = [];
    for (var i = 0; i < markersArray.length; i++) {
      markersData.push(markersArray[i].data);
    }

    var routeData = {
      routeName: routeName,
      markersData: markersData,
    };
    console.log(routeData);

    let options = {
      method: "POST",
      body: JSON.stringify(routeData),
      headers: {
        "Content-Type": "application/json",
      },
    };

    // TODO(#17): Handle response from fetch.
    await fetch("/storeRoute", options)
      .then((response) => response.json())
      .then((status) => {
        if (status.hasOwnProperty("errorMessage")) {
          alert(status.errorMessage);
        } else {
          console.log("receive new place's id " + status.toString());
        }
      });

    // Remove route details from the page.
    document.getElementById("places-table").innerHTML = "";
    document.getElementById("route-name").value = "";
    for (var i = 0; i < markersArray.length; i++) {
      markersArray[i].marker.setMap(null);
    }
    markersArray = [];
    alert(
      "Route successfully created!\nYou can see new created routes on your profile page!"
    );
  }
  loadRoutes();
}
