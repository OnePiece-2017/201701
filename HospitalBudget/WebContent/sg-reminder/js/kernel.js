var ______sgReminder = {
    wired : false,
    cache : {},
    callback : null,
    delay : 0
};

jQuery(document).ready(function() {
	if (!______sgReminder.wired) {
		// 1. 创建节点
		var html = '';
		html += '<div class="sg-reminder">';
		html += '	<div class="sg-reminder-contents">';
		html += '		<span class="the-text"></span>';
		html += '	</div>';
		html += '</div>';
		jQuery('body').prepend(html);
		// 2. 缓存节点
		______sgReminder.cache['master'] = jQuery('.sg-reminder');
		______sgReminder.cache['contents'] = jQuery('.sg-reminder-contents');
		______sgReminder.cache['theText'] = jQuery('.sg-reminder-contents span.the-text');
		// 3. 绑定事件
		______sgReminder.cache['master'].click(function() {
			______closePanel();
		});
		______sgReminder.cache['contents'].mouseover(function() {
			______sgReminder.cache['master'].stop();
			______sgReminder.cache['master'].css('opacity', 1);
		});
		______sgReminder.cache['contents'].mouseout(function() {
			______sgReminder.cache['master'].fadeOut(______sgReminder.delay, function() {
				______closePanel();
			});
		});
		// 4. 标记状态
		______sgReminder.wired = true;
	}
});

function ______closePanel() {
	______sgReminder.cache['master'].stop();
	______sgReminder.cache['master'].hide();
	______sgReminder.cache['theText'].html('');
	if (______sgReminder.callback != null && 'function' === typeof ______sgReminder.callback) {
		______sgReminder.callback();
	}
}

function ___sgReminder(text, delay, callback) {
	______sgReminder.callback = callback;
	if (null == text) {
		alert('sg-reminder调用失败，无效的text！');
	} else if (null == delay || '' === delay || 'number' !== typeof delay || parseInt(delay).toString() !== delay.toString() || delay < 0) {
		alert('sg-reminder调用失败，无效的delay！');
	} else {
		______sgReminder.delay = delay;
		______sgReminder.cache['master'].stop();
		______sgReminder.cache['theText'].html(text.toString());
		______sgReminder.cache['master'].css('opacity', 1);
		______sgReminder.cache['master'].show();
		______sgReminder.cache['master'].fadeOut(delay, function() {
			______closePanel();
		});
	}
}
