var ______promptMessage = {
    'isWired' : false,
    'refer' : {},
    'callback' : {},
    'iconset' : {
        'question' : (___contextRoot != '/' ? ___contextRoot : '') + '/prompt-message/img/icon_question_64x64.png',
        'success' : (___contextRoot != '/' ? ___contextRoot : '') + '/prompt-message/img/icon_success_64x64.png',
        'info' : (___contextRoot != '/' ? ___contextRoot : '') + '/prompt-message/img/icon_info_64x64.png',
        'warning' : (___contextRoot != '/' ? ___contextRoot : '') + '/prompt-message/img/icon_warning_64x64.png',
        'error' : (___contextRoot != '/' ? ___contextRoot : '') + '/prompt-message/img/icon_error_64x64.png'
    },
    'fadeInTimer' : null,
    'fadeOutTimer' : null
};

jQuery(document).ready(function() {
	// 1. 创建节点
	if (!______promptMessage['isWired']) {
		var closeIcon = (___contextRoot != '/' ? ___contextRoot : '') + '/prompt-message/img/icon_close_32x32.png';
		var html = '';
		html += '<div class="prompt-message">';
		html += '	<div class="prompt-message-contents">';
		html += '		<div class="btn-close">';
		html += '			<img src="' + closeIcon + '">';
		html += '		</div>';
		html += '		<div class="countdown">';
		html += '		</div>';
		html += '		<div class="logo">';
		html += '			<img src="' + ______promptMessage['iconset']['info'] + '">';
		html += '		</div>';
		html += '		<div class="title">';
		html += '			<span></span>';
		html += '		</div>';
		html += '		<div class="prompt">';
		html += '			<span></span>';
		html += '		</div>';
		html += '	</div>';
		html += '</div>';
		jQuery('body').prepend(html);
		______promptMessage['isWired'] = true;
	}
	// 2. 缓存节点
	______promptMessage['refer']['master'] = jQuery('.prompt-message');
	______promptMessage['refer']['prompt'] = jQuery('.prompt-message-contents .prompt');
	______promptMessage['refer']['countdown'] = jQuery('.prompt-message-contents .countdown');
	______promptMessage['refer']['btnClose'] = jQuery('.prompt-message-contents .btn-close');
	______promptMessage['refer']['logoImg'] = jQuery('.prompt-message-contents .logo img');
	// 3. 设置滚动条
	______promptMessage['refer']['prompt'].niceScroll({
	    cursorcolor : '#062121',
	    cursorwidth : 6,
	    cursoropacitymax : 0.8,
	    touchbehavior : false,
	    autohidemode : false
	});
	// 4. 绑定事件
	______promptMessage['refer']['master'].click(function() {
		clearTimeout(______promptMessage['fadeOutTimer']);
		______promptMessage['refer']['countdown'].stop();
		______promptMessage['refer']['countdown'].animate({
			width : 0
		}, 128, function() {
			______promptMessage['refer']['btnClose'].show();
		});
	});
	______promptMessage['refer']['btnClose'].click(function() {
		______promptMessage['refer']['master'].fadeOut('fast', function() {
			if (______promptMessage['callback']['fadeOutCallback'] != null && 'function' === typeof ______promptMessage['callback']['fadeOutCallback']) {
				______promptMessage['callback']['fadeOutCallback']();
			}
		});
	});
});

function ______promptMessageReset() {
	clearTimeout(______promptMessage['fadeInTimer']);
	clearTimeout(______promptMessage['fadeOutTimer']);
	______promptMessage['refer']['btnClose'].hide();
	______promptMessage['refer']['logoImg'].stop();
	______promptMessage['refer']['countdown'].stop();
	______promptMessage['refer']['countdown'].width(0);
	______promptMessage['refer']['logoImg'].attr('src', ______promptMessage['iconset']['info']);
	______promptMessage['refer']['logoImg'].css('opacity', '0');
	______promptMessage['refer']['master'].find('.title span').html('');
	______promptMessage['refer']['master'].find('.prompt span').html('');
	______promptMessage['refer']['prompt'].getNiceScroll().resize();
	______promptMessage['refer']['prompt'].getNiceScroll(0).doScrollTop(0, 0);
}

function ___promptMessage(arg) {
	______promptMessageReset();
	if (arg != null && 'object' === typeof arg && !arg.hasOwnProperty('length') && 'type' in arg && 'title' in arg && 'prompt' in arg && 'fadeInMillis' in arg && 'fadeOutMillis' in arg && 'fadeInCallback' in arg && 'fadeOutCallback' in arg) {
		var type = arg['type'], title = arg['title'], prompt = arg['prompt'], fadeInMillis = arg['fadeInMillis'], fadeOutMillis = arg['fadeOutMillis'], fadeInCallback = arg['fadeInCallback'], fadeOutCallback = arg['fadeOutCallback'];
		______promptMessage['callback']['fadeInCallback'] = fadeInCallback;
		______promptMessage['callback']['fadeOutCallback'] = fadeOutCallback;
		var logo = ______promptMessage['iconset'][type];
		if (null == logo || '' == logo) {
			logo = ______promptMessage['iconset']['info'];
		}
		______promptMessage['refer']['logoImg'].attr('src', logo);
		______promptMessage['refer']['logoImg'].animate({
			opacity : 1
		}, 512);
		______promptMessage['refer']['master'].find('.title span').html(title);
		______promptMessage['refer']['master'].find('.prompt span').html(prompt);
		______promptMessage['fadeInTimer'] = setTimeout(function() {
			______promptMessage['refer']['master'].fadeIn('fast', function() {
				if (fadeInCallback != null && 'function' === typeof fadeInCallback) {
					fadeInCallback();
				}
				______promptMessage['refer']['prompt'].getNiceScroll().resize();
				______promptMessage['refer']['prompt'].getNiceScroll(0).doScrollTop(0, 0);
				______promptMessage['refer']['countdown'].animate({
					width : jQuery('.prompt-message-contents').width()
				}, Number(fadeOutMillis));
				______promptMessage['fadeOutTimer'] = setTimeout(function() {
					______promptMessage['refer']['master'].fadeOut('fast', function() {
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

function ___promptMessageTiy() {
	___promptMessage({
	    type : 'warning',
	    title : '温馨提示',
	    prompt : '请输入验证码！<br><br><br><br><br><br>注意：验证码严格区分大小写！',
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
