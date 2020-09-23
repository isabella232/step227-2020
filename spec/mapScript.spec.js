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

const mapScript = require("../src/main/webapp/mapScript.js");

describe("createMarker function", function () {
  it("should add new value to the markersArray array", function () {
    var position = {
      lat: function () {
        return 35;
      },
      lng: function () {
        return 25;
      },
    };

    let marker = { position: position },
      place = { name: "Place", rating: 4 };
    globalIndex = 0;
    var markersArray = [];

    spyOn(mapScript, "createMarker").and.callFake(function (marker, place) {
      let id = globalIndex,
        stayHour = 0,
        stayMinute = 0,
        markerName = place.name;

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
    });
    mapScript.createMarker(marker, place);

    expect(markersArray.length).toBe(1);
  });
});

describe("findIndex function", function () {
  it("should return -1 when the id is not found", function () {
    var markersArray = [];

    expect(mapScript.findIndex(123, markersArray)).toEqual(-1);
  });
});

describe("findIndex function", function () {
  it("should return the associated index", function () {
    var markersArray = [];
    markersArray.push({
      marker: null,
      data: {
        id: 123,
      },
    });

    markersArray.push({
      marker: null,
      data: {
        id: 125,
      },
    });

    expect(mapScript.findIndex(125, markersArray)).toEqual(1);
  });
});

describe("deletePlaceFromArray function", function () {
  it("should delete the element with the associated id", function () {
    var marker = {
      setMap: function () {},
    };

    var markersArray = [];
    markersArray.push({
      marker: marker,
      data: {
        id: 123,
      },
    });

    markersArray.push({
      marker: marker,
      data: {
        id: 125,
      },
    });

    mapScript.deletePlaceFromArray(123, markersArray);
    expect(markersArray[0].data.id).toEqual(125);
  });
});
