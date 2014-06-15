$.ajax({
    url: 'http://api.kontaktynaurady.cz/api/v1/organizations?name=*Praha*',
    type: 'GET',
    dataType: 'jsonp',
    success: function(data) {
    	var container = $('<div></div>')
    	$.each(data, function(index, value) {
    		container.append($('<div>' + value.name + '</div>'));
    	});
    	
    	$('#container').html(container);
    }
});