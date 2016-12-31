// 字符串省略
function ___shortString(str) {
	return str;
}

// 文本截断
function ___textIntercept(element, maxlength) {
	jQuery(element).keyup(function() {
		if (jQuery(this).val().length > maxlength) {
			jQuery(this).val(jQuery(this).val().substring(0, maxlength));
		}
	});
	jQuery(element).blur(function() {
		jQuery(this).keyup();
	});
	jQuery(element).keyup();
}

// 文本截断ById
function ___textInterceptById(id, maxlength) {
	___textIntercept(___byId(id), maxlength);
}

// 文本限制
function ___textRestrict(element, maxlength) {
	if ('input' == element.tagName.toLowerCase() && ('text' == element.type.toLowerCase() || 'password' == element.type.toLowerCase())) {
		jQuery(element).attr("maxlength", maxlength);
	} else if ('textarea' == element.tagName.toLowerCase()) {
		___textIntercept(element, maxlength);
	}
}

// 文本限制ById
function ___textRestrictById(id, maxlength) {
	___textRestrict(___byId(id), maxlength);
}

// getElementById() 方法可返回对拥有指定 ID 的第一个对象的引用。
function ___byId(id) {
	return document.getElementById(id);
}

// getElementsByName() 方法可返回带有指定名称的对象的集合。NodeList
function ___byName(name) {
	return document.getElementsByName(name);
}

// getElementsByTagName() 方法可返回带有指定标签名的对象的集合。HTMLCollection
function ___byTagName(tagname) {
	return document.getElementsByTagName(tagname);
}

// ___placeholderSimulate
function ___placeholderSimulate(element) {
	;
}
