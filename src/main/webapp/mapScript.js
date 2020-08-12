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
  // Create markers.
  let japanTemple = { lat: 34.462117, lng: 135.830272 };
  let newZealandLake = { lat: -43.979931, lng: 170.194799 };
  let ugandaView = { lat: -0.099273, lng: 32.652921 };

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

  // Set markers on the map if marker flag is True.
  if (showMarkers) {
    let markerJapanTemple = new google.maps.Marker({
      position: japanTemple,
      map: map,
    });
    let markerNewZealand = new google.maps.Marker({
      position: newZealandLake,
      map: map,
    });
    let markerUganda = new google.maps.Marker({
      position: ugandaView,
      map: map,
    });
  }
  console.log("Initialise new map");
}
