jQuery(document).ready(function() {
	if (request_parameter != null && 'INVOKE_SUCCESS' == request_parameter['invoke_result']) {
		parent.sgFileupload.hideMaskLayer();
		var args = {
		    'assist_key' : 'gain_recently_uploaded',
		    'assist_value' : request_parameter['code']
		};
		jQuery.ajax({
		    type : 'POST',
		    dataType : 'jsonp',
		    jsonp : 'callback',
		    url : (___contextRoot != '/' ? ___contextRoot : '') + '/FileUploadAssistServlet',
		    data : {
			    'args' : JSON.stringify(args)
		    },
		    success : function(result) {
			    var callFunc = parent.sgFileupload.updateUploaded;
			    if (callFunc != null) {
				    callFunc(result);
			    } else {
				    alert('未绑定callFunc - updateUploaded');
			    }
		    },
		    error : function(err) {
			    alert('获取最近上传失败！');
		    }
		});
	} else {
		var args = {
		    'key' : 'gain_repository_data',
		    'value' : request_parameter['repositoryDataKey']
		};
		jQuery.ajax({
		    type : 'POST',
		    dataType : 'jsonp',
		    jsonp : 'callback',
		    url : (___contextRoot != '/' ? ___contextRoot : '') + '/GeneralServlet',
		    data : {
			    'args' : JSON.stringify(args)
		    },
		    success : function(result) {
			    if (result != null && 'INVOKE_SUCCESS' == result.invoke_result) {
				    var message = null;
				    try {
					    message = result.data.detailMessage + (top.toastr != null ? '<br>' : '\n');
				    } catch (e) {
					    message = '网络延迟，请稍后重试！';// 有效期太短validityDuration
				    }
				    if (top.toastr != null) {
					    top.toastr.options = {
					        'closeButton' : false,
					        'debug' : false,
					        'newestOnTop' : false,
					        'progressBar' : false,
					        'rtl' : false,
					        'positionClass' : 'toast-top-full-width',
					        'preventDuplicates' : false,
					        'onclick' : null,
					        'showDuration' : 300,
					        'hideDuration' : 1000,
					        'timeOut' : 3000,
					        'extendedTimeOut' : 1000,
					        'showEasing' : 'swing',
					        'hideEasing' : 'linear',
					        'showMethod' : 'fadeIn',
					        'hideMethod' : 'fadeOut'
					    };
					    top.toastr['error']('', message);
				    } else {
					    alert(message);
				    }
				    parent.sgFileupload.reloadDownload();
			    } else {
				    alert('请求失败！');
			    }
			    parent.sgFileupload.hideMaskLayer();
			    parent.sgFileupload.reloadController();
		    },
		    error : function(err) {
			    alert('gain_repository_data error!');
		    }
		});
	}
});

function messageProgressBar() {
	// ...for + '.' reset
	jQuery('.div-main .message').html();
}
