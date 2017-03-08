var ______promptInputbox = {
    wired : false,
    cache : {},
    handleIndex : 0,
    handleCurrent : null,
    threshold : {},
    timer : {}
};

jQuery(document).ready(function() {
	if (!______promptInputbox.wired) {
		// 1. 创建节点
		var html = '';
		html += '<div class="prompt-inputbox">';
		html += '	<div class="prompt-inputbox-contents">';
		html += '		<textarea class="textarea-bd"></textarea>';
		html += '		<div class="progress-bar">';
		html += '		</div>';
		html += '		<div class="progress-bar-details">';
		html += '			<span class="with-total"></span>';
		html += '			<span class="with-current"></span>';
		html += '		</div>';
		html += '		<div class="btn-operation">';
		html += '			<img class="img-yes" src="' + (___contextRoot != '/' ? ___contextRoot : '') + '/prompt-inputbox/img/icon_success_48x48.png' + '">';
		html += '			<img class="img-no" src="' + (___contextRoot != '/' ? ___contextRoot : '') + '/prompt-inputbox/img/icon_error_48x48.png' + '">';
		html += '		</div>';
		html += '		<div class="input-hint">';
		html += '			<span class="hint-text"></span>';
		html += '		</div>';
		html += '	</div>';
		html += '</div>';
		jQuery('body').prepend(html);
		// 2. 缓存节点
		______promptInputbox.cache['master'] = jQuery('.prompt-inputbox');
		______promptInputbox.cache['textarea'] = jQuery('.prompt-inputbox .textarea-bd');
		______promptInputbox.cache['progressBar'] = jQuery('.prompt-inputbox .progress-bar');
		______promptInputbox.cache['withCurrent'] = jQuery('.prompt-inputbox .with-current');
		______promptInputbox.cache['withTotal'] = jQuery('.prompt-inputbox .with-total');
		______promptInputbox.cache['inputHint'] = jQuery('.prompt-inputbox .input-hint');
		______promptInputbox.cache['hintText'] = jQuery('.prompt-inputbox .hint-text');
		______promptInputbox.cache['imgYes'] = jQuery('.prompt-inputbox .img-yes');
		______promptInputbox.cache['imgNo'] = jQuery('.prompt-inputbox .img-no');
		// 3. 设置滚动条
		______promptInputbox.cache['textarea'].niceScroll({
		    cursorcolor : '#062121',
		    cursorwidth : 8,
		    cursoropacitymax : 0.8,
		    touchbehavior : false,
		    autohidemode : true
		});
		// 4. 绑定事件
		______promptInputbox.cache['textarea'].keypress(function() {
			______promptInputboxNiceScrollResize();
		});
		______promptInputbox.cache['textarea'].keydown(function() {
			______promptInputboxNiceScrollResize();
		});
		______promptInputbox.cache['textarea'].keyup(function() {
			______promptInputboxNiceScrollResize();
		});
		______promptInputbox.cache['textarea'].mouseup(function() {
			______promptInputboxNiceScrollResize();
		});
		______promptInputbox.cache['imgYes'].click(function() {
			if (jQuery('.progress-bar').hasClass('be-overflow')) {
				______promptInputbox.cache['textarea'].focus();
				______promptInputboxHintTextTwinkle(8, 128);
			} else {
				clearTimeout(______promptInputbox.timer['tracker']);
				______promptInputboxHide();
				jQuery('[prompt-inputbox-handle-index="' + ______promptInputbox.handleCurrent + '"]').val(______promptInputbox.cache['textarea'].val()).focus();
			}
		});
		______promptInputbox.cache['imgNo'].click(function() {
			______promptInputboxHintTextTwinkle(0, 0);// 延迟过长时清除闪烁
			clearTimeout(______promptInputbox.timer['tracker']);
			______promptInputboxHide();
			jQuery('[prompt-inputbox-handle-index="' + ______promptInputbox.handleCurrent + '"]').focus();
		});
		// 5. 标记状态
		______promptInputbox.wired = true;
	}
});

function ______promptInputboxHintTextTwinkle(frequency, delay) {
	clearTimeout(______promptInputbox.timer['twinkle']);
	______promptInputbox.cache['hintText'].stop();
	______promptInputbox.cache['hintText'].css('opacity', 1);
	______promptInputbox.cache['hintText'].show();
	______promptInputboxHintTextTwinkleKernel(frequency, delay);
}

function ______promptInputboxHintTextTwinkleKernel(frequency, delay) {
	if (frequency > 0) {
		______promptInputbox.cache['hintText'].css('opacity', 0);
		______promptInputbox.cache['hintText'].animate({
			'opacity' : 1
		}, delay, function() {
			______promptInputbox.timer['twinkle'] = setTimeout('______promptInputboxHintTextTwinkleKernel(' + (frequency - 1) + ', ' + delay + ');', 0);
		});
	}
}

function ______promptInputboxShow() {
	______promptInputbox.cache['master'].show();
	______promptInputbox.cache['textarea'].focus();
}

function ______promptInputboxHide() {
	______promptInputbox.cache['master'].hide();
}

function ______promptInputboxNiceScrollResize() {
	______promptInputbox.cache['textarea'].getNiceScroll().resize();
}

function ______promptInputboxTracker() {
	var textLength = ______promptInputbox.cache['textarea'].val().length;
	______promptInputbox.cache['withCurrent'].html(textLength);
	var threshold = ______promptInputbox.threshold[______promptInputbox.handleCurrent];
	if (textLength > threshold) {
		______promptInputbox.cache['inputHint'].show();
		______promptInputbox.cache['progressBar'].addClass('be-overflow');
		______promptInputbox.cache['withCurrent'].addClass('with-current-overflow');
		______promptInputbox.cache['hintText'].html('已超出' + (textLength - threshold) + '个字');
	} else {
		if (threshold - textLength > 0) {
			______promptInputboxHintTextTwinkle(0, 0);// 延迟过长时清除闪烁
			______promptInputbox.cache['hintText'].html('可输入' + (threshold - textLength) + '个字');
			______promptInputbox.cache['inputHint'].show();
		} else {
			______promptInputboxHintTextTwinkle(0, 0);// 延迟过长时清除闪烁
			______promptInputbox.cache['inputHint'].hide();
		}
		______promptInputbox.cache['progressBar'].removeClass('be-overflow');
		______promptInputbox.cache['withCurrent'].removeClass('with-current-overflow');
	}
	if (threshold > 0 && textLength <= threshold) {
		______promptInputbox.cache['progressBar'].width(parseInt(textLength / threshold * 100) + '%');
	} else {
		______promptInputbox.cache['progressBar'].width('100%');
	}
	______promptInputbox.timer['tracker'] = setTimeout("______promptInputboxTracker()", 128);
}

function ___promptInputbox(id, threshold) {
	if (null == id || '' == id || 'string' !== typeof id) {
		alert('prompt-inputbox初始化失败，无效的id！');
	} else if (null == threshold || '' === threshold || 'number' !== typeof threshold || parseInt(threshold).toString() !== threshold.toString() || threshold < 0 || threshold > 2147483647) {
		alert('prompt-inputbox初始化失败，无效的threshold！');
	} else {
		______promptInputbox.handleIndex++;
		var element = document.getElementById(id);
		var eType = element.tagName.toLowerCase();
		if ('input' == eType || 'textarea' == eType) {
			jQuery(element).attr('prompt-inputbox-handle-index', ______promptInputbox.handleIndex);
			______promptInputbox.threshold[______promptInputbox.handleIndex] = threshold;
			jQuery(element).dblclick(function() {
				______promptInputbox.cache['textarea'].val('');
				______promptInputboxShow();
				______promptInputbox.cache['textarea'].val(this.value);
				______promptInputbox.handleCurrent = jQuery(this).attr('prompt-inputbox-handle-index');
				______promptInputbox.cache['withTotal'].html('/' + threshold);
				______promptInputbox.cache['progressBar'].hide();
				______promptInputbox.cache['progressBar'].width(0);
				______promptInputbox.cache['progressBar'].show();
				______promptInputboxTracker();
			});
		} else {
			alert('prompt-inputbox初始化失败，无效元素！' + id);
		}
	}
}
