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

  map.addListener("click", function (event) {
    placeMarker(event.latLng);
  });

  console.log("Initialise new map");

  async function placeMarker(location) {
    let marker = new google.maps.Marker({
      position: location,
      map: map,
    });

    let lat = location.lat(),
      lng = location.lng(),
      id = -1,
      visitHour = 0,
      visitMinute = 0,
      leaveHour = 0,
      leaveMinute = 0,
      markerName = "Place " + markersArray.length.toString();

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

    await fetch("/markers", options)
      .then((response) => response.json())
      .then((id) => {
        console.log("receive new place's id " + id.toString());
        markersArray.push({ marker: marker, id: id.toString() });

        addNewTableItem(markerName, id.toString());
        console.log("Place user's marker");
      });
    console.log("Store new marker");
  }
}

function loadRoute() {
  let options = {
    method: "GET",
  };

  fetch("/markers", options)
    .then((response) => response.json())
    .then((placesList) => {
      console.log("Get json of places");

      for (i in placesList) {
        let lat = placesList[i].lat,
          lng = placesList[i].lng,
          id = placesList[i].id;
        let position = { lat, lng };
        let marker = new google.maps.Marker({
          position: position,
          map: map,
        });
        markersArray.push({ marker: marker, id: id.toString() });

        addNewTableItem("Place name " + i.toString() + " ", id.toString());
      }

      console.log("Place markers");
    });
}

function addNewTableItem(name, id) {
  let tabel = document.getElementById("places-table");

  let newPlace = document.createElement("li");
  newPlace.id = id;
  newPlace.classList.add("new-place");

  let markerSign = '<span class="fas fa-ellipsis-v"></span>';
  let placeName = name;
  let deleteFunction = "deletePlace('" + id + "')";
  let deleteSign =
    '<span class="fas fa-minus-square" onclick="' +
    deleteFunction +
    '"></span>';

  newPlace.innerHTML = markerSign + placeName + deleteSign;

  tabel.appendChild(newPlace);
}

async function deletePlace(contentId) {
  let tableItem = document.getElementById(contentId);
  tableItem.parentNode.removeChild(tableItem);
  console.log("Remove place from the table");

  for (var i = 0; i < markersArray.length; i++) {
    if (markersArray[i].id == contentId) {
      markersArray[i].marker.setMap(null);
      markersArray.splice(i, 1);
      console.log("remove marker from the array and the map");
      break;
    }
  }

  let options = {
    method: "POST",
  };

  let URL = "/delete_marker?contentId=" + contentId;
  await fetch(URL, options)
    .then((response) => response.json())
    .then((deletionResult) => {
      if (deletionResult) {
        console.log("Successful deletion");
      } else {
        console.log("Unsuccessful deletion");
      }
    });
}
