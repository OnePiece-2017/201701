jQuery(document).ready(function() {
	var args = {
	    'key' : 'pull_repository_data',
	    'value' : repositoryDataKey
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
				    message = '通信异常，请稍后再试！';// 文件记录存在，物理文件不存在，有效期太短
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
		    alert('pull_repository_data error!');
	    }
	});
});
