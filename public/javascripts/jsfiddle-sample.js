$.ajax({
    url: 'http://kontaktynauradyapi-databox.rhcloud.com/api/v1/organizations?name=*Praha*',
    type: 'GET',
    dataType: 'jsonp',
    success: function(data) {
    	var container = $('<div></div>')
    	$.each(data, function(index, value) {
    		container.append($('<div>' + value + '</div>'));
    	});
    	
    	$('#container').html(container);
    }
});