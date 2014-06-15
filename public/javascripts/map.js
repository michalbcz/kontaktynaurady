$(function() {
	
	var map = initializeMap();

	function initializeMap() {
		var mapOptions = {
				center: new google.maps.LatLng(50.075, 14.437),
				zoom: 11,
				mapTypeId: google.maps.MapTypeId.ROADMAP
		};
		return new google.maps.Map(document.getElementById("map-canvas"), mapOptions);
	}

	function displayOrganizations(data) {
		$.each(data, function(index, value) {

            var infowindow = new google.maps.InfoWindow();

			var marker = new google.maps.Marker({
			    position: new google.maps.LatLng(value.latitude, value.longitude),
			    map: map,
			    title: value.name
			});

            google.maps.event.addListener(marker, 'click', function() {
                infowindow.setContent(
                                value.name + "<br/>" + value.addressStreet + "<br/>" + value.addressZipCode + " " + value.addressCity + "<br/>" +
                                "<a href=\"" + value.www + "\">" + value.www + "</a>"  + " / " + "<a href=\"mailto:" + value.email + "\">" + value.email + "</a>");
                infowindow.open(map, this);
            });
			
		});
	}
	
	$.get("/api/v1/organizations?name=*Praha*", function(data) {
		displayOrganizations(data);
	});
	
});