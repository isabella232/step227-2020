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
    center: new google.maps.LatLng(0.211772, 102.290621),
    zoom: 2.3,
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
      lng = location.lng();

    let data = { lat, lng };
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

        addNewTableItem("New place name", id.toString());
        console.log("Place user's marker");
      });
    console.log("Store new marker");
  }
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
  await fetch(URL, options).then();
}

async function createRoute() {
  var routeName = document.getElementById("route-name").value;
  if (routeName == "") {
    alert("Please add a name to your new route!");
  } else {
    var markersIds = [];
    for (var i = 0; i < markersArray.length; i++) {
      markersIds.push({ id: markersArray[i].id });
    }
    let options = {
      method: "POST",
      body: JSON.stringify(markersIds),
      headers: {
        "Content-Type": "application/json",
      },
    };

    let URL = "/storeRoute?routeName=" + routeName;
    await fetch(URL, options).then();

    let tabel = document.getElementById("places-table");
    tabel.innerHTML = "";
    for (var i = 0; i < markersArray.length; i++) {
      markersArray[i].marker.setMap(null);
    }
    alert(
      "Route successfully created!\nYou can see new created routes on your profile page!"
    );
  }
}
