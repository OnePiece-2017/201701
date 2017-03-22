var ______sgInputbox = {
    wired : false,
    alias : {},
    eType : {},
    notify : {},
    cache : {},
    handleIndex : 0,
    handleCurrent : null,
    threshold : {},
    timer : {},
    allowEnter : {},
    magicBinder : {},
    reminderService : true
};

jQuery(document).ready(function() {
	if (!______sgInputbox.wired) {
		// 1. 创建节点
		var html = '';
		html += '<div class="sg-inputbox">';
		html += '	<div class="sg-inputbox-contents">';
		html += '		<textarea class="textarea-bd"></textarea>';
		html += '		<div class="progress-bar">';
		html += '		</div>';
		html += '		<div class="progress-bar-details">';
		html += '			<span class="with-total"></span>';
		html += '			<span class="with-current"></span>';
		html += '		</div>';
		html += '		<div class="btn-operation">';
		html += '			<img class="img-yes" src="' + (___contextRoot != '/' ? ___contextRoot : '') + '/sg-inputbox/img/icon_success_48x48.png' + '">';
		html += '			<img class="img-no" src="' + (___contextRoot != '/' ? ___contextRoot : '') + '/sg-inputbox/img/icon_error_48x48.png' + '">';
		html += '		</div>';
		html += '		<div class="input-hint">';
		html += '			<span class="hint-text"></span>';
		html += '		</div>';
		html += '	</div>';
		html += '</div>';
		jQuery('body').prepend(html);
		// 2. 缓存节点
		______sgInputbox.cache['master'] = jQuery('.sg-inputbox');
		______sgInputbox.cache['textarea'] = jQuery('.sg-inputbox-contents .textarea-bd');
		______sgInputbox.cache['progressBar'] = jQuery('.sg-inputbox-contents .progress-bar');
		______sgInputbox.cache['withCurrent'] = jQuery('.sg-inputbox-contents .progress-bar-details .with-current');
		______sgInputbox.cache['withTotal'] = jQuery('.sg-inputbox-contents .progress-bar-details .with-total');
		______sgInputbox.cache['hintText'] = jQuery('.sg-inputbox-contents .input-hint .hint-text');
		______sgInputbox.cache['imgYes'] = jQuery('.sg-inputbox-contents .btn-operation .img-yes');
		______sgInputbox.cache['imgNo'] = jQuery('.sg-inputbox-contents .btn-operation .img-no');
		// 3. 设置滚动条
		______sgInputbox.cache['textarea'].niceScroll({
		    cursorcolor : '#062121',
		    cursorwidth : 8,
		    cursoropacitymax : 0.8,
		    touchbehavior : false,
		    autohidemode : true
		});
		// 4. 绑定事件
		______sgInputbox.cache['textarea'].keypress(function() {
			______resizeNiceScroll();
		});
		______sgInputbox.cache['textarea'].keydown(function(event) {
			if (!______sgInputbox.allowEnter[______sgInputbox.handleCurrent] && 13 == event.which) {
				___sgReminder(______gainElementTypeLabel(______sgInputbox.eType[______sgInputbox.handleCurrent]) + '﹝' + ______sgInputbox.alias[______sgInputbox.handleCurrent] + '﹞不支持回车符！', 1024, function() {
					______sgInputbox.cache['textarea'].focus();
				});
				return false;
			}
			if (9 == event.which) {
				if ('?' == 'Shift is holding') {
					if ('?' == 'Cursor front is Tab') {
						// TODO: ?
					} else {
						// TODO: ?
					}
				} else {
					// TODO: ?
				}
			}
			______resizeNiceScroll();
		});
		______sgInputbox.cache['textarea'].keyup(function() {
			______resizeNiceScroll();
		});
		______sgInputbox.cache['textarea'].mouseup(function() {
			______resizeNiceScroll();
		});
		______sgInputbox.cache['imgYes'].click(function() {
			if (jQuery('.progress-bar').hasClass('be-overflow')) {
				______sgInputbox.cache['textarea'].focus();
				______hintTextTwinkle(8, 128);
			} else {
				clearTimeout(______sgInputbox.timer['tracker']);
				______hidePanel();
				var refer = jQuery('[sg-inputbox-handle-index="' + ______sgInputbox.handleCurrent + '"]');
				if (______sgInputbox.notify[______sgInputbox.handleCurrent]) {
					refer.unbind('focus');
				}
				refer.val(______sgInputbox.cache['textarea'].val());
				refer.focus();
				if (______sgInputbox.notify[______sgInputbox.handleCurrent]) {
					refer.focus(______sgInputbox.magicBinder[______sgInputbox.handleCurrent]);
				}
			}
		});
		______sgInputbox.cache['imgNo'].click(function() {
			______hintTextTwinkle(0, 0);// 延迟过长时清除闪烁
			clearTimeout(______sgInputbox.timer['tracker']);
			______hidePanel();
			var refer = jQuery('[sg-inputbox-handle-index="' + ______sgInputbox.handleCurrent + '"]');
			if (______sgInputbox.notify[______sgInputbox.handleCurrent]) {
				refer.unbind('focus');
			}
			refer.focus();
			if (______sgInputbox.notify[______sgInputbox.handleCurrent]) {
				refer.focus(______sgInputbox.magicBinder[______sgInputbox.handleCurrent]);
			}
		});
		// 5. 标记状态
		______sgInputbox.wired = true;
	}
});

function ______hintTextTwinkle(frequency, delay) {
	clearTimeout(______sgInputbox.timer['twinkle']);
	______sgInputbox.cache['hintText'].stop();
	______sgInputbox.cache['hintText'].css('opacity', 1);
	______sgInputbox.cache['hintText'].show();
	______hintTextTwinkleKernel(frequency, delay);
}

function ______hintTextTwinkleKernel(frequency, delay) {
	if (frequency > 0) {
		______sgInputbox.cache['hintText'].css('opacity', 0);
		______sgInputbox.cache['hintText'].animate({
			'opacity' : 1
		}, delay, function() {
			______sgInputbox.timer['twinkle'] = setTimeout('______hintTextTwinkleKernel(' + (frequency - 1) + ', ' + delay + ');', 0);
		});
	}
}

function ______showPanel() {
	______sgInputbox.cache['master'].show();
	______sgInputbox.cache['textarea'].focus();
}

function ______hidePanel() {
	______sgInputbox.cache['master'].hide();
}

function ______resizeNiceScroll() {
	______sgInputbox.cache['textarea'].getNiceScroll().resize();
}

function ______typingTracker() {
	var textLength = ______sgInputbox.cache['textarea'].val().length;
	______sgInputbox.cache['withCurrent'].html(textLength);
	var threshold = ______sgInputbox.threshold[______sgInputbox.handleCurrent];
	if (textLength > threshold) {
		______sgInputbox.cache['hintText'].addClass('beyond');
		______sgInputbox.cache['progressBar'].addClass('be-overflow');
		______sgInputbox.cache['withCurrent'].addClass('with-current-overflow');
		______sgInputbox.cache['hintText'].html('已超出' + (textLength - threshold) + '个字');
	} else {
		if (threshold - textLength > 0) {
			______hintTextTwinkle(0, 0);// 延迟过长时清除闪烁
			______sgInputbox.cache['hintText'].html('可输入' + (threshold - textLength) + '个字');
		} else {
			______hintTextTwinkle(0, 0);// 延迟过长时清除闪烁
			______sgInputbox.cache['hintText'].html('输入已达到上限');
		}
		______sgInputbox.cache['progressBar'].removeClass('be-overflow');
		______sgInputbox.cache['withCurrent'].removeClass('with-current-overflow');
		______sgInputbox.cache['hintText'].removeClass('beyond');
	}
	if (threshold > 0 && textLength <= threshold) {
		______sgInputbox.cache['progressBar'].width((textLength / threshold * 100).toFixed(6) + '%');
	} else {
		______sgInputbox.cache['progressBar'].width('100%');
	}
	______sgInputbox.timer['tracker'] = setTimeout("______typingTracker()", 128);
}

function ______gainElementTypeLabel(eType) {
	return 'input' == eType ? '文本字段' : '文本区';
}

function ___sgInputboxStartReminderService() {
	______sgInputbox.reminderService = true;
}

function ___sgInputboxStopReminderService() {
	______sgInputbox.reminderService = false;
}

function ___sgInputbox(arg) {
	if (arg != null && 'object' === typeof arg && !arg.hasOwnProperty('length') && 'id' in arg && 'alias' in arg && 'threshold' in arg && 'notify' in arg) {
		var id = arg['id'], alias = arg['alias'], threshold = arg['threshold'], notify = arg['notify'];
		if (null == id || '' == id || 'string' !== typeof id || null == document.getElementById(id)) {
			alert('sg-inputbox初始化失败，无效的id！');
		} else if (null == alias || '' == alias || 'string' !== typeof alias) {
			alert('sg-inputbox初始化失败，无效的alias！');
		} else if (null == threshold || '' === threshold || 'number' !== typeof threshold || parseInt(threshold).toString() !== threshold.toString() || threshold < 0 || threshold > 2147483647) {
			alert('sg-inputbox初始化失败，无效的threshold！');
		} else if (null == notify || 'boolean' !== typeof notify) {
			alert('sg-inputbox初始化失败，无效的notify！');
		} else {
			______sgInputbox.handleIndex++;
			var element = document.getElementById(id);
			var eType = element.tagName.toLowerCase();
			if ('input' == eType || 'textarea' == eType) {
				jQuery(element).attr('sg-inputbox-handle-index', ______sgInputbox.handleIndex);
				______sgInputbox.alias[______sgInputbox.handleIndex] = alias;
				______sgInputbox.threshold[______sgInputbox.handleIndex] = threshold;
				______sgInputbox.notify[______sgInputbox.handleIndex] = notify;
				______sgInputbox.allowEnter[______sgInputbox.handleIndex] = 'textarea' == eType;
				______sgInputbox.eType[______sgInputbox.handleIndex] = eType;
				if (notify) {
					var magicBinder = null;
					magicBinder = function() {
						if (______sgInputbox.reminderService) {
							___sgReminder('亲：待提示完全消失后，可双击鼠标左键来打开迷你输入框。<br>' + ______gainElementTypeLabel(eType) + '﹝' + alias + '﹞', 2048, function() {
								jQuery(element).unbind('focus');
								jQuery(element).focus();
								jQuery(element).focus(magicBinder);
							});
						}
					};
					______sgInputbox.magicBinder[______sgInputbox.handleIndex] = magicBinder;
					jQuery(element).focus(magicBinder);
				}
				jQuery(element).dblclick(function() {
					______sgInputbox.cache['textarea'].val('');
					______showPanel();
					______sgInputbox.cache['textarea'].val(this.value);
					______sgInputbox.cache['textarea'].getNiceScroll().resize();
					______sgInputbox.cache['textarea'].getNiceScroll(0).doScrollTop(______sgInputbox.cache['textarea'][0].scrollHeight, 0);
					______sgInputbox.handleCurrent = jQuery(this).attr('sg-inputbox-handle-index');
					______sgInputbox.cache['withTotal'].html('/' + threshold);
					______sgInputbox.cache['progressBar'].hide();
					______sgInputbox.cache['progressBar'].width(0);
					______sgInputbox.cache['progressBar'].show();
					______typingTracker();
				});
			} else {
				alert('sg-inputbox初始化失败，无效元素！' + id);
			}
		}
	} else {
		alert('sg-inputbox初始化失败，参数无效！');
	}
}

function sgInputboxTiy() {
	___sgInputbox({
	    id : 'mainForm:theDescription',
	    alias : '描述',
	    threshold : 1024,
	    notify : true
	});
}
