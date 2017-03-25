名称：
	文件上传

用法：
	sgFileupload['install']({
	    'target' : 'avatar',
	    'alias' : '头像',
	    'source' : jQuery('#mainForm\\:avatarHidden').val(),
	    'class' : 'sg-fu-custom--xxx',
	    'completed' : function() {
		    alert('附件安装成功！');
	    }
	});

注意：
	target的类型如果是span则class默认为preset
