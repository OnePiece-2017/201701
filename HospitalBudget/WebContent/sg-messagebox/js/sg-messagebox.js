var ______sgMessagebox = {
    isWired : false,
    cache : {},
    callback : {},
    iconset : {
        'question' : (___contextRoot != '/' ? ___contextRoot : '') + '/sg-messagebox/img/icon_question_64x64.png',
        'success' : (___contextRoot != '/' ? ___contextRoot : '') + '/sg-messagebox/img/icon_success_64x64.png',
        'info' : (___contextRoot != '/' ? ___contextRoot : '') + '/sg-messagebox/img/icon_info_64x64.png',
        'warning' : (___contextRoot != '/' ? ___contextRoot : '') + '/sg-messagebox/img/icon_warning_64x64.png',
        'error' : (___contextRoot != '/' ? ___contextRoot : '') + '/sg-messagebox/img/icon_error_64x64.png'
    },
    fadeInTimer : null,
    fadeOutTimer : null
};

jQuery(document).ready(function() {
	// 1. 创建节点
	if (!______sgMessagebox.isWired) {
		var closeIcon = (___contextRoot != '/' ? ___contextRoot : '') + '/sg-messagebox/img/icon_close_32x32.png';
		var html = '';
		html += '<div class="sg-messagebox">';
		html += '	<div class="sg-messagebox-contents">';
		html += '		<div class="btn-close">';
		html += '			<img src="' + closeIcon + '">';
		html += '		</div>';
		html += '		<div class="countdown">';
		html += '		</div>';
		html += '		<div class="logo">';
		html += '			<img src="' + ______sgMessagebox.iconset['info'] + '">';
		html += '		</div>';
		html += '		<div class="title">';
		html += '			<span></span>';
		html += '		</div>';
		html += '		<div class="message">';
		html += '			<span></span>';
		html += '		</div>';
		html += '	</div>';
		html += '</div>';
		jQuery('body').prepend(html);
		______sgMessagebox.isWired = true;
	}
	// 2. 缓存节点
	______sgMessagebox.cache['master'] = jQuery('.sg-messagebox');
	______sgMessagebox.cache['message'] = jQuery('.sg-messagebox-contents .message');
	______sgMessagebox.cache['countdown'] = jQuery('.sg-messagebox-contents .countdown');
	______sgMessagebox.cache['btnClose'] = jQuery('.sg-messagebox-contents .btn-close');
	______sgMessagebox.cache['logoImg'] = jQuery('.sg-messagebox-contents .logo img');
	// 3. 设置滚动条
	______sgMessagebox.cache['message'].niceScroll({
	    cursorcolor : '#062121',
	    cursorwidth : 6,
	    cursoropacitymax : 0.8,
	    touchbehavior : false,
	    autohidemode : false
	});
	// 4. 绑定事件
	______sgMessagebox.cache['master'].click(function() {
		clearTimeout(______sgMessagebox.fadeOutTimer);
		______sgMessagebox.cache['countdown'].stop();
		______sgMessagebox.cache['countdown'].animate({
			width : 0
		}, 128, function() {
			______sgMessagebox.cache['btnClose'].show();
		});
	});
	______sgMessagebox.cache['btnClose'].click(function() {
		______sgMessagebox.cache['master'].fadeOut('fast', function() {
			if (______sgMessagebox.callback['fadeOutCallback'] != null && 'function' === typeof ______sgMessagebox.callback['fadeOutCallback']) {
				______sgMessagebox.callback['fadeOutCallback']();
			}
		});
	});
});

function ______sgMessageboxReset() {
	clearTimeout(______sgMessagebox.fadeInTimer);
	clearTimeout(______sgMessagebox.fadeOutTimer);
	______sgMessagebox.cache['btnClose'].hide();
	______sgMessagebox.cache['logoImg'].stop();
	______sgMessagebox.cache['countdown'].stop();
	______sgMessagebox.cache['countdown'].width(0);
	______sgMessagebox.cache['logoImg'].attr('src', ______sgMessagebox.iconset['info']);
	______sgMessagebox.cache['logoImg'].css('opacity', '0');
	______sgMessagebox.cache['master'].find('.title span').html('');
	______sgMessagebox.cache['master'].find('.message span').html('');
	______sgMessagebox.cache['message'].getNiceScroll().resize();
	______sgMessagebox.cache['message'].getNiceScroll(0).doScrollTop(0, 0);
}

function ___sgMessagebox(arg) {
	______sgMessageboxReset();
	if (arg != null && 'object' === typeof arg && !arg.hasOwnProperty('length') && 'type' in arg && 'title' in arg && 'message' in arg && 'fadeInMillis' in arg && 'fadeOutMillis' in arg && 'fadeInCallback' in arg && 'fadeOutCallback' in arg) {
		var type = arg['type'], title = arg['title'], message = arg['message'], fadeInMillis = arg['fadeInMillis'], fadeOutMillis = arg['fadeOutMillis'], fadeInCallback = arg['fadeInCallback'], fadeOutCallback = arg['fadeOutCallback'];
		______sgMessagebox.callback['fadeInCallback'] = fadeInCallback;
		______sgMessagebox.callback['fadeOutCallback'] = fadeOutCallback;
		var logo = ______sgMessagebox.iconset[type];
		if (null == logo || '' == logo) {
			logo = ______sgMessagebox.iconset['info'];
		}
		______sgMessagebox.cache['logoImg'].attr('src', logo);
		______sgMessagebox.cache['logoImg'].animate({
			opacity : 1
		}, 512);
		______sgMessagebox.cache['master'].find('.title span').html(title);
		______sgMessagebox.cache['master'].find('.message span').html(message);
		______sgMessagebox.fadeInTimer = setTimeout(function() {
			______sgMessagebox.cache['master'].fadeIn('fast', function() {
				if (fadeInCallback != null && 'function' === typeof fadeInCallback) {
					fadeInCallback();
				}
				______sgMessagebox.cache['message'].getNiceScroll().resize();
				______sgMessagebox.cache['message'].getNiceScroll(0).doScrollTop(0, 0);
				______sgMessagebox.cache['countdown'].animate({
					width : jQuery('.sg-messagebox-contents').width()
				}, Number(fadeOutMillis));
				______sgMessagebox.fadeOutTimer = setTimeout(function() {
					______sgMessagebox.cache['master'].fadeOut('fast', function() {
						if (fadeOutCallback != null && 'function' === typeof fadeOutCallback) {
							fadeOutCallback();
						}
					});
				}, Number(fadeOutMillis));
			});
		}, Number(fadeInMillis));
	} else {
		alert('参数无效！');
	}
}

function sgMessageboxTiy() {
	___sgMessagebox({
	    type : 'warning',
	    title : '温馨提示',
	    message : '请输入验证码！<br><br><br><br><br><br>注意：验证码严格区分大小写！',
	    fadeInMillis : 0,
	    fadeOutMillis : 1000 * 3,
	    fadeInCallback : function() {
		    jQuery('#verification_code').val('');
	    },
	    fadeOutCallback : function() {
		    jQuery('#verification_code').focus();
	    }
	});
}
