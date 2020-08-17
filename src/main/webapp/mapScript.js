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

function initMap(showMarkers = true) {
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

  function placeMarker(location) {
    let marker = new google.maps.Marker({
      position: location,
      map: map,
    });

    let lat = location.lat(),
      lng = location.lng();

    let data = { lat, lng };
    console.log("data");
    console.log(data);
    let options = {
      method: "POST",
      body: JSON.stringify(data),
      headers: {
        "Content-Type": "application/json",
      },
    };

    fetch("/markers", options)
      .then((response) => response.json())
      .then((id) => {
        console.log("receive new place's id");
        console.log(id);

        addNewTableItem("New place name", id.toString());

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
        console.log("Place " + i.toString());
        console.log(placesList[i].id);
        let lat = placesList[i].lat,
          lng = placesList[i].lng,
          id = placesList[i].id;
        let position = { lat, lng };
        let marker = new google.maps.Marker({
          position: position,
          map: map,
        });

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

function deletePlace(contentId) {
  console.log(contentId);
  let tableItem = document.getElementById(contentId);
  tableItem.parentNode.removeChild(tableItem);
  console.log("Remove place from the table");

  let options = {
    method: "POST",
  };

  let URL = "/delete_marker?contentId=" + contentId;
  fetch(URL, options).then();
}
