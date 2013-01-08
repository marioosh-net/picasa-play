function loading(a){
	$(a).html('<div style="padding: 5px;"><img src="/assets/images/ajax-loader7.gif"/>&#160;loading...</div>');
}

var xhr;
function exif(id) {
	loading('#exif');
	xhr = $.ajax({
		url: '/exif/'+'exiftool/'+id+'/0',
		beforeSend: function() {
			if(xhr && xhr.readystate != 4){
	            xhr.abort();
	        }		
		},
		success: function(data){
			$('#exif').html(data);
		}
	});
}

function exif(index, albumid, photoid) {
	loading('#exif');
	xhr = $.ajax({
		beforeSend: function() {
			if(xhr && xhr.readystate != 4){
	            xhr.abort();
	        }		
		},		
		url: '/exif/'+index+'/'+albumid+'/'+photoid
	}).done(function(data){
		/*$('#debug').show();
		$('#debug').html(data);*/
		$('#exif').html(data);
	});
}

function scrollRefresh() {
	$('#albums-only').css('height','500px');
	var e = $('#albums-only').jScrollPane();
	var api = e.data('jsp');
	if(!api.getIsScrollableV()) {
		api.destroy();
		$('#albums-only').css('height','auto');
	}	
}

function filter(s) {
	/*$('#debug').html(''); $('#debug').show();*/
	$('#albums-only a').each(function(){
		if($(this).attr('title').toUpperCase().indexOf(s.toUpperCase()) >= 0) {
			/*$('#debug').html($('#debug').html()+ ' GOOD: ' + $(this).attr('title'));*/
			if(!$('#bpub').hasClass('on') || ($('#bpub').hasClass('on') && $(this).hasClass('pub'))) {
				$(this).show();
			} else {
				$(this).hide();
			}
		} else {
			/*$('#debug').html($('#debug').html()+ ' NOT: ' + $(this).attr('title'));*/
			$(this).hide();
		}
	});
	scrollRefresh();
	
	$('.covers .thumbs .thumb_box').each(function(){
		if($(this).attr('title').toUpperCase().indexOf(s.toUpperCase()) >= 0) {
			/*$('#debug').html($('#debug').html()+ ' GOOD: ' + $(this).attr('title'));*/
			$(this).show();
		} else {
			/*$('#debug').html($('#debug').html()+ ' NOT: ' + $(this).attr('title'));*/
			$(this).hide();
		}
	});
	
}

function loadingIcon(a){
	$(a).html("<img src='/assets/images/ajax-loader7.gif'/>")
}