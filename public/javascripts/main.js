function loading(a){
	jQuery(a).html('<div style="padding: 5px;"><img src="/assets/images/ajax-loader7.gif"/>&#160;loading...</div>');
}

var xhr;
function exif(id) {
	loading('#exif');
	xhr = jQuery.ajax({
		url: '/exif/'+'exiftool/'+id+'/0',
		beforeSend: function() {
			if(xhr && xhr.readystate != 4){
	            xhr.abort();
	        }		
		},
		success: function(data){
			jQuery('#exif').html(data);
		}
	});
}
