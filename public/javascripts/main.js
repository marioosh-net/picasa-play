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
	
}

function loadingIcon(a){
	$(a).html("<img src='/assets/images/ajax-loader7.gif'/>");
}

function msg(m) {
	$(function(){
	    $('#msg')
    	.html(m)
    	.show()
    	.delay(2000)
    	.slideUp(300);		
	});
}

function vis(_this, url, album) {
	if($(_this).parent().find('.loading').length == 0) {
		$(_this).parent().append("<img class='loading' src='/assets/images/ajax-loader7.gif'/>");
	}
	$(_this).parent().find('.loading').show();
	$(_this).hide();
	$.ajax({url: url}).done(function(r) {
		$(_this).parent().find('.loading').hide();
		if(r == '0') {
			$(_this).parent().find('.priv').hide();
			$(_this).parent().find('.pub').show();
			if(album) {
				$('div.album-selected').parent().removeClass('pub'); $('div.album-selected').find('span.pubi').hide();
			}
		} else {
			$(_this).parent().find('.priv').show();
			$(_this).parent().find('.pub').hide();
			if(album) {
				$('div.album-selected').parent().addClass('pub'); $('div.album-selected').find('span.pubi').show();
			}
		}
	}); 
	return false;
}