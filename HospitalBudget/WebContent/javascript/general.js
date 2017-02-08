var ___handle = (function() {
	return {
	    cookie : function() {
	    },
	    memory : function() {
	    }
	};
})();

var ___trash = null; // 回收站
var ___message_set = {
    '' : '',
    'warn' : '严重警告',
    'prompt' : '温馨提示'
}; // 消息集合

var ___logger = {
    info : null,
    warn : null,
    error : null,
    print : function() {
	    if (this.info != null) {
		    ___trash = 'info: ' + this.info;
	    }
	    if (this.warn != null) {
		    ___trash = 'warn: ' + this.warn;
	    }
	    if (this.error != null) {
		    ___trash = 'error: ' + this.error;
	    }
    }
}; // 日志

var ___dynamic_function = null; // 动态函数

var ___remodal_fn_def = {
    opening : function() {
	    ___logger.info = 'opening';
	    ___logger.print();
    },
    opened : function() {
	    ___logger.info = 'opened';
	    ___logger.print();
    },
    closing : function(e) {
	    ___logger.info = 'closing' + (e.reason ? ', reason: ' + e.reason : '');
	    ___logger.print();
    },
    closed : function(e) {
	    ___logger.info = 'closed' + (e.reason ? ', reason: ' + e.reason : '');
	    ___logger.print();
    },
    confirmation : function() {
	    ___logger.info = 'confirmation';
	    ___logger.print();
    },
    cancellation : function() {
	    ___logger.info = 'cancellation';
	    ___logger.print();
    }
}; // Remodal默认函数

var ___remodal_fn_set = {
    _default : ___remodal_fn_def,
    _custom : null
}; // Remodal函数集

jQuery(document).ready(function() {
	___trash = '在文档加载后激活函数 http://www.w3school.com.cn/jquery/event_ready.asp';
});

// 對JavaScript Date对象添加属性和方法
Date.prototype.format = function() {
	var fullYear = this.getFullYear();
	var month = this.getMonth() + 1;
	var date = this.getDate();
	var hours = this.getHours();
	var minutes = this.getMinutes();
	var seconds = this.getSeconds();
	var day = this.getDay();
	var dayArr = [ '星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六' ];
	// var milliseconds = this.getMilliseconds();
	return fullYear + '年' + month + '月' + date + '日　' + dayArr[day] + '　' + (hours > 9 ? hours : '0' + hours) + ':' + (minutes > 9 ? minutes : '0' + minutes) + ':' + (seconds > 9 ? seconds : '0' + seconds);
};

function ___ask(title, description, fn) {
	jQuery('#modal_ask__title').html(title);
	jQuery('#modal_ask__desc').html(description);
	___remodal_fn_set['_custom'] = fn;
	window.location.href = '#modal_ask';
}

function ___msg(title, description, fn) {
	jQuery('#modal_msg__title').html(title);
	jQuery('#modal_msg__desc').html(description);
	___remodal_fn_set['_custom'] = fn;
	window.location.href = '#modal_msg';
}

function ___abbr(source, length) {
	var abbreviation = '';
	if (source != null && source != '') {
		if (source.length <= length) {
			abbreviation = source;
		} else if (length < 1) {
			abbreviation = source.substring(0, length);
		} else {
			abbreviation = source.substring(0, length - 1) + '…';
		}
	}
	return abbreviation;
}

function ___recursionObject() {
	console.log(arguments);
	// 目標：遞歸一個對象，如果該對象中包含對象，則繼續遞歸該對象，或是調用其方法，或是輸出其屬性等等。。。
}

function ___wrapStr() {
	var result = '';
	for (var i = 0; i < arguments.length; i++) {
		result += '[' + arguments[i] + ']';
		if (i < arguments.length - 1) {
			result += ',';
		}
	}
	return result;
}

function ___log(arg) {
	console.log(arg);
}

function ___constantValue(constantKey) {
	return {
	    // 浏览器类型
	    BROWSER_UNKNOWN : 0,
	    BROWSER_FIREFOX : 1,
	    BROWSER_CHROME : 2,
	    BROWSER_EDGE : 3,
	    BROWSER_MSIE : 4,
	    BROWSER_SAFARI : 5,
	    BROWSER_OPERA : 6,
	    // 消息类型
	    MESSAGE_UNKNOWN : 0,
	    MESSAGE_SUCCESS : 1,
	    MESSAGE_INFO : 2,
	    MESSAGE_WARNING : 3,
	    MESSAGE_ERROR : 4
	}[constantKey];
}

function ___constantName(constantKey) {
	return {
	    // 浏览器类型
	    BROWSER_UNKNOWN : '?',
	    BROWSER_FIREFOX : 'Firefox',
	    BROWSER_CHROME : 'Chrome',
	    BROWSER_EDGE : 'Edge',
	    BROWSER_MSIE : 'IE',
	    BROWSER_SAFARI : 'Safari',
	    BROWSER_OPERA : 'Opera',
	    // 消息类型
	    MESSAGE_UNKNOWN : '?',
	    MESSAGE_SUCCESS : '成功',
	    MESSAGE_INFO : '提示',
	    MESSAGE_WARNING : '警告',
	    MESSAGE_ERROR : '错误'
	}[constantKey];
}

function ___browserType() {
	var ua = navigator.userAgent;// 浏览器的用户代理报头
	if (ua != null && ua != '') {
		ua = ua.toUpperCase();
		if (ua.indexOf('Firefox'.toUpperCase()) > -1) {// 火狐浏览器 Mozilla/5.0 (Windows NT 6.1; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0
			return ___constantValue('BROWSER_FIREFOX');
		} else if (ua.indexOf('Chrome'.toUpperCase()) > -1) {// 谷歌浏览器 Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36
			return ___constantValue('BROWSER_CHROME');;
		} else if (ua.indexOf('MSIE'.toUpperCase()) > -1) {// IE浏览器 Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; .NET4.0C; .NET4.0E)
			return ___constantValue('BROWSER_MSIE');;
		}
	} else {
		return ___constantValue('BROWSER_UNKNOWN');;
	}
}

function ___expression() {
	try {
		alert("Result:" + eval(prompt("Enter an expression:", "")));
	} catch (exception) {
		alert(exception);
	}
}

function ___sleep(millis) {
	if (millis != null && millis > 0) {
		var start = new Date().getTime();
		while (new Date().getTime() - start < millis) {
		}
	}
}

function ___isNumber(arg) {
	return arg != null && arg !== '' && Number(arg).toString() === arg.toString();
}

function ___genStdHtml___a_abbr(value) {
	return '<a class="a-abbr" href="javascript:void(0);" title="' + value + '">' + ___abbr(value, 19) + '</a>';
}
