var ______BRIEF_INTRODUCTION = null;// 平台核心纯js，不依赖jQuery
var ______FUNCTION_INTRODUCTION = null;// 仅用于书写注释

______FUNCTION_INTRODUCTION = null;// 通知main页面已加载完毕
var myName = window.name;// 当前页面所属iframe的name属性
if (myName != null && myName != '') {
	var dataArr = myName.split(':');
	if (dataArr != null && 2 == dataArr.length) {
		var handle = parent[dataArr[0]];
		if (handle != null && 'function' === typeof handle) {
			handle(dataArr[1]);
		}
	} else {
		alert('#ERROR: Invalid iframe name.');
	}
}
