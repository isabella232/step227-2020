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
let directionsService;
let directionsRenderer;
var globalIndex = 0;
var markersArray = [];
var editorsArray = [];
var viewersArray = [];
var invalidPlace = {
  name: "Invalid",
  formatted_address: "Invalid",
  rating: 0,
};

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

function addSearchBar() {
  const input = document.getElementById("pac-input");
  const searchBox = new google.maps.places.SearchBox(input);
  // Bias the SearchBox results towards current map's viewport.
  map.controls[google.maps.ControlPosition.TOP_LEFT].push(input);

  map.addListener("bounds_changed", () => {
    searchBox.setBounds(map.getBounds());
  });

  searchBox.addListener("places_changed", () => {
    const places = searchBox.getPlaces();

    if (places.length == 0) {
      return;
    }

    const bounds = new google.maps.LatLngBounds();
    places.forEach((place) => {
      if (!place.geometry) {
        console.log("Returned place contains no geometry");
        return;
      }

      if (place.geometry.viewport) {
        // Only geocodes have viewport.
        bounds.union(place.geometry.viewport);
      } else {
        bounds.extend(place.geometry.location);
      }
    });
    map.fitBounds(bounds);
  });
}

function findPlaceId(location, placeIdFunction) {
  var geocoder = new google.maps.Geocoder();
  geocoder.geocode({ location: location }, function (results, status) {
    placeId = -1;
    if (status === "OK") {
      if (results[0]) {
        console.log(results[0].place_id);
        placeId = results[0].place_id;
      } else {
        console.log("No results found");
      }
    } else {
      console.log("Geocoder failed due to: " + status);
    }
    placeIdFunction(placeId);
  });
}

function findPlaceDetails(placeId, placeDetailsFunction) {
  var request = {
    placeId: placeId,
    fields: ["name", "formatted_address", "rating"],
  };

  service = new google.maps.places.PlacesService(map);
  service.getDetails(request, (place, status) => {
    if (status === google.maps.places.PlacesServiceStatus.OK) {
      placeDetailsFunction(place);
    } else {
      console.log("Failed due to" + status);
      placeDetailsFunction(invalidPlace);
    }
  });
}

function showSteps(directionResult, waypoints) {
  // For each step, place a marker, and add the text to the marker's infowindow.
  // Also attach the marker to an array so we can keep track of it and remove it
  // when calculating new routes.
  const myRoute = directionResult.routes[0].legs[0];

  for (let i = 0; i < myRoute.steps.length; i++) {
    const marker = (waypoints[i] = waypoints[i] || new google.maps.Marker());
    marker.setMap(map);
    marker.setPosition(myRoute.steps[i].start_location);
  }
}

function calculateAndDisplayRoute(
  directionsRenderer,
  directionsService,
  waypoints,
  originLatLng,
  destinationLatLng
) {
  // First, remove any existing markers from the map.
  for (let i = 0; i < markersArray.length; i++) {
    markersArray[i].marker.setMap(null);
  }

  directionsService.route(
    {
      origin: originLatLng,
      destination: destinationLatLng,
      travelMode: google.maps.TravelMode.TRANSIT,
    },
    (result, status) => {
      // Route the directions and pass the response to a function to create
      // markers for each step.
      if (status === "OK") {
        directionsRenderer.setDirections(result);
        showSteps(result, waypoints);
      } else {
        window.alert("Directions request failed due to " + status);
      }
    }
  );
}

function initRoute() {
  directionsService = new google.maps.DirectionsService();
  directionsRenderer = new google.maps.DirectionsRenderer({ map: map });
}

function drawRoute() {
  let originLatLng = new google.maps.LatLng({
    lat: markersArray[0].data.lat,
    lng: markersArray[0].data.lng,
  });
  let destinationLatLng = new google.maps.LatLng({
    lat: markersArray[markersArray.length - 1].data.lat,
    lng: markersArray[markersArray.length - 1].data.lng,
  });

  let waypoints = [];
  for (var i = 1; i < markersArray.length - 1; i++) {
    waypoints.push(markersArray[i].marker);
  }

  calculateAndDisplayRoute(
    directionsRenderer,
    directionsService,
    waypoints,
    originLatLng,
    destinationLatLng
  );
  console.log(waypoints);

  /*let transitOptions = {
    arrivalTime: Date
  }*/
}

function initMap() {
  initInactiveMap();
  addSearchBar();
  initRoute();

  checkLog().then((loggedIn) => {
    if (loggedIn == true) {
      map.addListener("click", function (event) {
        placeMarker(event.latLng);
      });

      function placeMarker(location) {
        let marker = new google.maps.Marker({
          position: location,
          map: map,
        });

        findPlaceId(location, function placeId(placeId) {
          if (placeId == -1) {
            createMarker(marker, invalidPlace);
          } else {
            findPlaceDetails(placeId, function placeDetails(place) {
              createMarker(marker, place);
            });
          }
        });
      }
    }
  });
}

function createMarker(marker, place) {
  let id = globalIndex,
    stayHour = 0,
    stayMinute = 0,
    markerName = place.name;

  console.log(place.rating);

  markersArray.push({
    marker: marker,
    data: {
      id: id,
      lat: marker.position.lat(),
      lng: marker.position.lng(),
      stayHour: stayHour,
      stayMinute: stayMinute,
      markerName: markerName,
      rating: place.rating,
    },
  });

  // Create table item for marker.
  addNewTableItem(markerName, id.toString());
  globalIndex = globalIndex + 1;

  // Add info window for marker.
  const infowindow = new google.maps.InfoWindow();
  infowindow.setContent(
    "<div><strong>" +
      place.name +
      "</strong><br>" +
      "<span>Rating: </span>" +
      place.rating +
      "<br>" +
      place.formatted_address +
      "</div>"
  );
  infowindow.open(map, marker);
  google.maps.event.addListener(marker, "click", function () {
    infowindow.open(map, this);
  });
  initRoute();
  drawRoute();
}

function addNewTableItem(name, placeId) {
  let tabel = document.getElementById("places-table");

  let newPlace = document.createElement("li");
  newPlace.id = "place" + placeId;
  newPlace.classList.add("new-place");

  let markerSign = '<span class="fas fa-ellipsis-v"></span>';
  let placeName = '<span id="place-name">' + name + "</span>";
  let deleteFunction = "deletePlace('" + placeId + "')";
  let showSelectFunction = "showSettings('" + placeId + "')";
  let settingsButton =
    '<span class="fas fa-cog" onclick="' + showSelectFunction + '"></span>';
  let deleteSign =
    '<span class="fas fa-minus-square" onclick="' +
    deleteFunction +
    '"></span>';

  newPlace.innerHTML = markerSign + placeName + deleteSign + settingsButton;

  tabel.appendChild(newPlace);
}

function findIndex(placeId) {
  for (var i = 0; i < markersArray.length; i++) {
    if (markersArray[i].data.id == placeId) {
      return i;
    }
  }
  return -1;
}

function deletePlace(placeId) {
  let tableItem = document.getElementById("place" + placeId);
  tableItem.parentNode.removeChild(tableItem);

  var actualIndex = findIndex(placeId);
  if (actualIndex != -1) {
    markersArray[actualIndex].marker.setMap(null);
    markersArray.splice(actualIndex, 1);
  }
}

function showSettings(placeId) {
  var actualIndex = findIndex(placeId);
  if (actualIndex != -1) {
    var settings = document.getElementsByClassName("marker-setting")[0];
    document.getElementById("submit-button").onclick = function () {
      updateMarkerSettings(placeId);
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
}

function updateMarkerSettings(placeId) {
  let markerName = document.getElementById("marker-name").value,
    stayHour = document.getElementById("stay-hour").value,
    stayMinute = document.getElementById("stay-minute").value;

  var actualIndex = findIndex(placeId);
  if (actualIndex != -1) {
    markersArray[actualIndex].data.markerName = markerName;
    markersArray[actualIndex].data.stayHour = stayHour;
    markersArray[actualIndex].data.stayMinute = stayMinute;

    let tableItemElemens = document.getElementById("place" + placeId)
      .childNodes;
    tableItemElemens[1].innerHTML = markerName;
  }
}

function createRouteData() {
  let routeId = 0,
    routeName = document.getElementById("route-name").value,
    isPublic = Boolean(document.getElementById("publicity").value == 1),
    startHour = document.getElementById("start-hour").value,
    startMinute = document.getElementById("start-minute").value,
    isCompleted = false,
    numberOfRatings = markersArray.length;

  var markersData = [];
  var sumOfRatings = 0;
  for (var i = 0; i < markersArray.length; i++) {
    let rating = markersArray[i].data.rating;
    if (rating != undefined) {
      sumOfRatings += rating;
    } else {
      markersArray[i].data.rating = 0;
    }

    markersData.push(markersArray[i].data);
  }

  var routeData = {
    routeId: routeId,
    routeName: routeName,
    routeMarkers: markersData,
    isPublic: isPublic,
    isCompleted: isCompleted,
    startHour: startHour,
    startMinute: startMinute,
    editorsArray: editorsArray,
    numberOfRatings: numberOfRatings,
    sumOfRatings: sumOfRatings,
    status: "NEW",
  };
  console.log(routeData);
  return routeData;
}

async function createRoute() {
  let routeData = createRouteData();
  if (routeData.routeName == "") {
    alert("Please add a name to your new route!");
  } else {
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
        if (jsonResponse.success && jsonResponse.object.isPublic) {
          let routesGrid = document.getElementById("routes-grid");
          if (routesGrid.innerHTML == "No suggestions available!") {
            routesGrid.innerHTML = "";
          }
          routesGrid.appendChild(createRouteCard(jsonResponse.object));
        }
        alert(jsonResponse.message);
      });
    removeRouteInfo();
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

// Show an area to share a route with friends.
function showShareSection() {
  var section = document.getElementById("share-section");
  section.classList.toggle("show");
}

function editMode(route) {
  removeRouteInfo();
  initMap();
  markersArray = [];
  directionsRenderer.setMap(null);
  globalIndex = route.routeMarkers.length;

  // Change submit button properties.
  let submitButton = document.getElementById("create-route-button");
  submitButton.innerHTML = "SAVE CHANGES";
  submitButton.style.visibility = "visible";
  submitButton.onclick = function () {
    editRoute(route);
  };

  // Update route details.
  let routeName = document.getElementById("route-name");
  routeName.value = route.routeName;

  document.getElementById("start-hour").value = route.startHour;
  document.getElementById("start-minute").value = route.startMinute;
  document.getElementById("share-with-friends-button").style.visibility =
    "visible";
  if (route.isPublic) {
    publicRoute();
  } else {
    privateRoute();
  }

  // Fill the table and add markers on the map.
  for (var i = 0; i < route.routeMarkers.length; i++) {
    let markerData = route.routeMarkers[i];
    addNewTableItem(markerData.markerName, i);

    let marker = new google.maps.Marker({
      position: {
        lat: route.routeMarkers[i].lat,
        lng: route.routeMarkers[i].lng,
      },
      map: map,
    });

    markersArray.push({
      marker: marker,
      data: {
        id: i,
        lat: markerData.lat,
        lng: markerData.lng,
        stayHour: markerData.stayHour,
        stayMinute: markerData.stayMinute,
        markerName: markerData.markerName,
        rating: markerData.rating,
      },
    });
  }
  initRoute();
  drawRoute();

  // Add button to exit edit mode.
  let backButton = document.createElement("button");
  backButton.innerHTML = "BACK TO ROUTE CREATION";
  backButton.onclick = function () {
    location.reload();
  };
  document.getElementById("additional").innerHTML = "";
  document.getElementById("additional").appendChild(backButton);
}

async function editRoute(route) {
  let routeData = createRouteData();
  routeData.routeId = route.routeId;
  routeData.status = "EDIT";

  if (routeData.routeName == "") {
    alert("Please add a name to your new route!");
  } else {
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
        alert(jsonResponse.message);
      });
  }
}
