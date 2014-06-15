$(function() {
	
	var map = initializeMap();

	function initializeMap() {
		var mapOptions = {
				center: new google.maps.LatLng(50.075, 14.437),
				zoom: 5,
				mapTypeId: google.maps.MapTypeId.ROADMAP
		};
		return new google.maps.Map(document.getElementById("map-canvas"), mapOptions);
	}

	function displayOrganizations(data) {
		$.each(data, function(index, value) {
			
			var marker = new google.maps.Marker({
			    position: new google.maps.LatLng(value.latitude, value.longitude),
			    map: map,
			    title: value.name
			});
			
		});
	}
	
	$.get("/api/v1/organizations?name=*Praha*", function(data) {
		displayOrganizations(data);
	});
	
});