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

  checkLog().then((loggedIn) => {
    if (loggedIn == true) {
      map.addListener("click", function (event) {
        placeMarker(event.latLng);
      });

      console.log("Initialise new map");

      async function placeMarker(location) {
        let marker = new google.maps.Marker({
          position: location,
          map: map,
        });

        let index = markersArray.length;
        markersArray.push({ marker: marker, index: index.toString() });
        addNewTableItem("New place name", index.toString());
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
  let placeName = name;
  let deleteFunction = "deletePlace('" + index + "')";
  let deleteSign =
    '<span class="fas fa-minus-square" onclick="' +
    deleteFunction +
    '"></span>';

  newPlace.innerHTML = markerSign + placeName + deleteSign;

  tabel.appendChild(newPlace);
}

function deletePlace(contentId) {
  let tableItem = document.getElementById("place" + contentId);
  tableItem.parentNode.removeChild(tableItem);
  console.log("Remove place from the table");

  for (var i = 0; i < markersArray.length; i++) {
    if (markersArray[i].index == contentId) {
      markersArray[i].marker.setMap(null);
      markersArray.splice(i, 1);
      console.log("remove marker from the array and the map");
      break;
    }
  }
}

async function createRoute() {
  var routeName = document.getElementById("route-name").value;
  if (routeName == "") {
    alert("Please add a name to your new route!");
  } else {
    var markersIds = [];
    console.log(markersArray);

    for (var i = 0; i < markersArray.length; i++) {
      let lat = markersArray[i].marker.position.lat(),
        lng = markersArray[i].marker.position.lng();

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
          markersIds.push({ id: id });
        });
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

    document.getElementById("places-table").innerHTML = "";
    document.getElementById("route-name").value = "";
    for (var i = 0; i < markersArray.length; i++) {
      markersArray[i].marker.setMap(null);
    }
    alert(
      "Route successfully created!\nYou can see new created routes on your profile page!"
    );
  }
}
