jQuery(document).ready(function() {
	if (request_parameter != null && 'INVOKE_SUCCESS' == request_parameter['invoke_result']) {
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
					    message = result.data.detailMessage + (top.toastr != null ? '<br>' : '\n') + '文件名〔' + result.data.targetFile + '〕';
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
		    },
		    error : function(err) {
			    alert('gain_repository_data error!');
		    }
		});
	} else {// 已达到峰值REPOSITORY_DATA_MAX_SIZE
		var message = '目前服务器压力过大，请稍后重试！';
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
	}
});
