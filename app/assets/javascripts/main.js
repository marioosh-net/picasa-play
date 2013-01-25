function search() {	
	$('#search-progress').show(); 
	filter($('#search').val()); 
	$('#search-progress').hide(); 
	$('#search').focus();
	$('#search').select(); 
}
$(function(){
	/*$('#albums-only').jScrollPane();*/
	scrollRefresh();
    
	$("#bsearch").click(function(event) { search(); });
    $("#ball").click(function(event) { $('#search').val(''); search(); });	                
    $("#search").change(function(event) { search(); });
    $("#bpub").click(function(event) { $(this).toggleClass('on'); search(); });
    $("#search").keypress(function(event) { if (event.which == 13) { search(); } });
    
    $('.openalbum').click(function(){
    	openalbum($(this).attr('url'), $(this).attr('selector'));
    });
    photosEventHandlers(false);

});

function loading(a){
	$(a).html('<div style="padding: 5px;"><img src="/assets/images/ajax-loader7.gif"/>&#160;loading...</div>');
}

var photosEventHandlers = function(t){
	if(window.location.hash == '#direct' || t) {
		$('#exif-container').css('height', '200px');
	}
    $('.showexif').mouseover(function(){
    	exif($(this).attr('si'), $(this).attr('ai'), $(this).attr('pi'));	
    });		
    $('.page').click(function(){
    	loadingIcon(this); 
    	$('#right').load($(this).attr('url'), function(){
    		photosEventHandlers(true);
    	}); 
    	return false;	    	
    });
    
    $('.vis').click(function(){
    	vis($(this),$(this).attr('url'),$(this).attr('al'));
    });

    /**
     * Lightview
     */
    $('.lv').bind('click', function(event) {
    	event.preventDefault();
    	var hrefs = $('.lv').map(function() { return $(this).attr('href'); }).get();
    	var thumbs = $('.lv').map(function() { return $(this).attr('thumbnail'); }).get();
    	var titles = $('.lv').map(function() { return $(this).attr('data-lightview-title'); }).get();
    	var e = new Array();
    	$.each(hrefs, function(i, value) {
    		e[i] = 
    			{
    				url: value, 
    				title: titles[i],
    				options: {
    					thumbnail: thumbs[i]
    				} 
    			};
    	});
    	/*alert($.toJSON(e));*/
    	Lightview.show(e, {
    		type: 'image',
    		controls: 'thumbnails', 
    		slideshow: 3000,
    		shadow: true,
    		afterUpdate: function(element, position) {
    			/* dosn't work: title/caption element not rendered yet */
    		    $('.vis-title').click(function(){
    		    	vis($(this),$(this).attr('url'),$(this).attr('al'));
    		    });
    		}	
    	});
    });    
    
};		

/**
 * preserve name temporarily
 * becacause of can't register handler dymaically (title/caption element not rendered yet)
 */
window['visc'] = visc
function visc(e) {
	vis($(e),$(e).attr('url'),$(e).attr('al'));
}

function openalbum(url, selector) {
	$('.oneset').removeClass('album-selected'); 
	$(selector).addClass('album-selected'); 
	$('#exif').html(''); 
	loading('#right'); 
	$('#right').load(url, function(){
		photosEventHandlers(true);
	}); 
	return false;	
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
