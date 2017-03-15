//===================菜单模块开始===================
var menuInfoJsonArray = null;
var tabContainer = {};
var userClickTab = false;
// ===================菜单模块结束===================

// ===================timeout开始===================
var timeout_ctrl = null;
var timeout_userFollowing = null;
var timeout_serverInfo = null;
// ===================timeout结束===================

// ===================jeasyui tabs开始===================
var tabPanelCtrl = {
    selectedKey : null,
    allowClose : true
};
var tempfunction = null;
var tabIndex = 0;
var tablKeyPrefix = 'jeasyui_tabs__';
// ===================jeasyui tabs结束===================

// ===================通行证数据开始===================
var ______userInfoId = null;// 用户id
var ______userInfoIdMD5 = null;// 用户idMD5
var ______reloadPageOnExist = false;// reload_page_on_exist
var ______askOnCloseLabel = false;// ask_on_close_label
var ______remoteClock = new Date();// 远程时钟
var ______requestRemoteClockBegin = 0;// 请求远程时钟开始毫秒数
var ______requestRemoteClockFunction = 0;// 递归调用函数
// ===================通行证数据结束===================

jQuery(document).ready(function() {
	jQuery(window).resize(function() {
		selfAdaptionScreen();
	});

	jQuery('.div-main-left').niceScroll({
	    cursorcolor : '#4d0acf',
	    cursorwidth : 6,
	    cursoropacitymax : 0.8,
	    touchbehavior : false,
	    autohidemode : true
	});

	jQuery('body').niceScroll({
	    cursorcolor : '#86cef0',
	    cursorwidth : 8,
	    cursoropacitymax : 0.8,
	    touchbehavior : false,
	    autohidemode : false
	});

	setTimeout(selfAdaptionScreen, 0);// 异步自适应屏幕（Chrome某些元素渲染顺序不同步）

	var funcCode = '';// 菜单HTML代码
	var disposeLeaf = function(leaf) {
		for (var i = 0; i < leaf.length; i++) {
			var node = leaf[i];
			funcCode += '<li><div class="func-outer"><div class="func-inner">';
			funcCode += '<span class="func-logo"' + (node['iconSrc'] != null && node['iconSrc'] != '' ? ' style="background: RGBA(0, 0, 0, 0) url(' + node['iconSrc'] + ') no-repeat center;"' : '') + '></span>';
			funcCode += '<span class="func-name"';
			if (0 == node['leaf'].length) {
				tabIndex++;// 索引自增
				funcCode += ' tab-index="' + tabIndex + '"';
				tabContainer[tabIndex] = {
				    'index' : tabIndex,
				    'key' : md5(String(tabIndex)),
				    'url' : node['tabUrl'],
				    'title' : node['tabTitle'],
				    'value' : node['theValue']
				};
			}
			funcCode += '>' + node['theValue'] + '</span>';
			if (node['leaf'].length > 0) {
				funcCode += '<span class="toggle collapse"></span></div></div><ul style="display: none;">';
				disposeLeaf(node['leaf']);
				funcCode += '</ul>';
			} else if (node['tabUrl'] != null && node['tabUrl'] != '') {
				funcCode += '</div></div>';
			} else {
				funcCode += '<span class="func-unopened" title="该功能处于停用状态！"></span></div></div>';
			}
			funcCode += '</li>';
		}
	};
	if (menuInfoJsonArray != null) {
		disposeLeaf(menuInfoJsonArray);
	}
	jQuery('.function-container').html(funcCode);
	jQuery('.function-container .func-outer').click(function() {
		jQuery('.div-main-left .mask-layer').show();
		var cache_ul = jQuery(this).next('ul');
		if (cache_ul.length > 0) {
			jQuery(this).find('.toggle').toggleClass(function() {
				if (jQuery(this).hasClass('collapse')) {
					jQuery(this).removeClass('collapse');
					return 'expand';
				} else {
					jQuery(this).removeClass('expand');
					return 'collapse';
				}
			});
			cache_ul.slideToggle(256, function() {
				jQuery('.div-main-left .mask-layer').hide();
				jQuery('.div-main-left').getNiceScroll().resize();
			});
		} else {
			var tcRefer = tabContainer[jQuery(this).find('.func-name').attr('tab-index')];
			var key = tcRefer['key'];// 选项卡键
			var url = tcRefer['url'];// 选项卡链接
			var title = tcRefer['title'];// 选项卡标题
			var value = tcRefer['value'];// 菜单名称
			if (url != null && url != '') {
				userClickTab = true;
				if (title != null && title != '') {
					addTabPanel(key, ___abbr(title, 10), url);
				} else {
					addTabPanel(key, ___abbr(value, 10), url);
				}
				userClickTab = false;
				setTimeout(function() {
					jQuery('.div-main-left .mask-layer').hide();
					jQuery('.div-main-left').getNiceScroll().resize();
				}, 256);// 防止恶意点击
			} else {
				toastr.options = {
				    'closeButton' : false,
				    'debug' : false,
				    'newestOnTop' : false,
				    'progressBar' : false,
				    'rtl' : false,
				    'positionClass' : 'toast-bottom-right',
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
				toastr['error']('温馨提示', '该功能处于停用状态！');
				setTimeout(function() {
					jQuery('.div-main-left .mask-layer').hide();
				}, 256);// 防止恶意点击
			}
		}
	});

	var nodeCrud = jQuery('#node_crud');
	nodeCrud.tabs({
	    onSelect : function(title, index) {
		    var tabId = nodeCrud.tabs('getTab', index)[0].id;
		    var tabKey = tabId.substring(tablKeyPrefix.length);
		    jQuery('.div-main-left .function-container .func-inner.highlight').removeClass('highlight');
		    for ( var key in tabContainer) {
			    if (tabContainer[key]['key'] == tabKey) {
				    var activated = jQuery('.div-main-left .function-container .func-name[tab-index="' + tabContainer[key]['index'] + '"]').parents('.func-inner');
				    activated.addClass('highlight');
				    var funcOuterRefer = activated.parents('ul').prev('.func-outer');
				    if (funcOuterRefer != null && funcOuterRefer.length > 0) {
					    for (var i = funcOuterRefer.length - 1; i >= 0; --i) {
						    if (jQuery(funcOuterRefer[i]).next('ul').is(':hidden')) {
							    jQuery(funcOuterRefer[i]).next('ul').show();
							    jQuery(funcOuterRefer[i]).find('.toggle').removeClass('collapse').addClass('expand');
						    }
					    }
				    }
				    if (!userClickTab) {
					    gotoAppropriatePosition();
				    }
				    break;
			    }
		    }
	    },
	    onUpdate : function(title, index) {
		    var tabId = nodeCrud.tabs('getTab', index)[0].id;
		    var tabRef = jQuery('#' + tabId);
		    tabRef.addClass('jeasyui-tabs-loading-wrapper');
		    tabRef.prepend('<div class="jeasyui-tabs-loading-contents"></div>');
	    },
	    onBeforeClose : function(title, index) {
		    if (______askOnCloseLabel && !confirm('确定要关闭选项卡（' + title + '）吗？')) {
			    return false;
		    }
		    tabPanelCtrl.selectedKey = nodeCrud.tabs('getSelected')[0].id;
		    return tabPanelCtrl.allowClose;
	    },
	    onClose : function(title, index) {
		    tabPanelCtrl.allowClose = false;
		    var tabPanelIndex = gainTabPanelIndex(tabPanelCtrl.selectedKey);
		    tabPanelIndex = tabPanelIndex > -1 ? tabPanelIndex : 0;
		    if (nodeCrud.tabs('tabs').length > 0) {
			    tempfunction = function() {
				    nodeCrud.tabs('select', tabPanelIndex);
				    tabPanelCtrl.allowClose = true;
			    };
			    clearTimeout(timeout_ctrl);
			    timeout_ctrl = setTimeout("tempfunction();", 0);
		    } else {
			    jQuery('.div-main-left .function-container .func-inner.highlight').removeClass('highlight');
			    nodeCrud.hide();
			    jQuery('#node_welcome').show();
			    tabPanelCtrl.allowClose = true;
		    }
		    tabPanelCtrl.selectedKey = null;
	    }
	});
	// TODO: session restore
	if (0 == nodeCrud.tabs('tabs').length) {
		nodeCrud.hide();
		jQuery('#node_welcome').show();
	}

	// timeout_userFollowing = setTimeout('requestUserFollowingServlet()', 0);

	setTimeout('requestServerInfoServlet()', 0);// 异步请求远程时钟
});

function gotoAppropriatePosition() {
	if (jQuery('.div-main-left .function-container .func-inner.highlight').length > 0) {
		jQuery('.div-main-left').getNiceScroll().resize();// 重新调整下拉框大小
		var offsetContainer = jQuery('.div-main-left .function-container .func-outer:first').offset();
		var offsetHighlight = jQuery('.div-main-left .function-container .func-inner.highlight').parents('.func-outer').offset();
		jQuery('.div-main-left').getNiceScroll(0).doScrollLeft(offsetHighlight.left - offsetContainer.left, 0);
		jQuery('.div-main-left').getNiceScroll(0).doScrollTop(offsetHighlight.top - offsetContainer.top, 0); // 定位到最佳位置
	}
}

function selfAdaptionScreen() {
	var mainWidth = jQuery('.div-main').width();
	var mainHeight = jQuery('.div-main').height();
	var subtrahendWidth = mainHeight > 600 ? 192 : 202;
	var subtrahendHeight = mainWidth > 800 ? 60 : 70;
	jQuery('.div-main-left').css('height', mainHeight - subtrahendHeight);
	jQuery('.div-main-left .mask-layer').css('height', mainHeight - subtrahendHeight);
	jQuery('.div-main-right').css('width', mainWidth - subtrahendWidth);
	jQuery('.div-main-right').css('height', mainHeight - subtrahendHeight);
	jQuery('body').getNiceScroll().resize();
	jQuery('body').getNiceScroll(0).doScrollLeft(0, 0);
	jQuery('body').getNiceScroll(0).doScrollTop(0, 0);
	gotoAppropriatePosition();
}

function requestServerInfoServlet() {
	______requestRemoteClockBegin = new Date().getTime();// 设置请求远程时钟开始时间毫秒数
	jQuery.ajax({
	    type : 'POST',
	    dataType : 'text',
	    url : (___contextRoot != '/' ? ___contextRoot : '') + '/ServerInfoServlet?type=1',
	    success : function(result) {
		    if (result != null && result != '') {
			    ______requestRemoteClockFunction = function(time) {
				    clearTimeout(timeout_serverInfo);
				    if (new Date().getTime() - ______requestRemoteClockBegin > 1000 * 60) {
					    jQuery('#remoteClock').html('更新中...');
					    requestServerInfoServlet();// 为避免受CPU影响而造成误差，故在一个周期之外再次发起请求
				    } else {
					    ______remoteClock.setTime(time);
					    jQuery('#remoteClock').html(______remoteClock.format());
					    timeout_serverInfo = setTimeout('______requestRemoteClockFunction(' + (time + 1000) + ')', 1000);// 間隔一個周期重新發起請求
				    }
			    };
			    var delayed = new Date().getTime() - ______requestRemoteClockBegin;
			    ______requestRemoteClockFunction(Number(result) + delayed);// 计入请求延误时间
		    } else {
			    jQuery('#remoteClock').html('加载失败！');
		    }
	    },
	    error : function(err) {
		    jQuery('#remoteClock').html('加载失败！');
	    }
	});
}

function requestUserFollowingServlet() {
	// 优化代码 setTimeout 返回值存储 自动计算下一次上报状态间隔时间
	var args = {
		'userInfoIdMD5' : ______userInfoIdMD5
	};
	jQuery.ajax({
	    type : 'POST',
	    dataType : 'jsonp',
	    jsonp : 'callback',
	    url : (___contextRoot != '/' ? ___contextRoot : '') + '/UserFollowingServlet',
	    data : {
		    'args' : JSON.stringify(args)
	    },
	    success : function(result) {
		    console.log("version --> " + JSON.stringify(result));
		    if (result != null && result.version != '') {
			    timeout_userFollowing = setTimeout('requestUserFollowingServlet()', 2000);
		    } else {
			    clearTimeout(timeout_userFollowing);
			    alert('服务器拒绝了您的请求！');
			    banUse();
		    }
	    },
	    error : function(err) {
		    clearTimeout(timeout_userFollowing);
		    console.log(err);
		    alert('与服务器通信失败！');
		    banUse();
	    }
	});
}

function banUse() {
	alert('哎呀，不好！');
	window.location.href = '/logout.xhtml';
}

function addTabPanel(key, title, url) {
	if (key != undefined && key != null && key != '') {
		key = tablKeyPrefix + key;
		var nodeCrud = jQuery('#node_crud');
		if (nodeCrud.is(':hidden')) {
			jQuery('#node_welcome').hide();
			nodeCrud.show();
		}
		___log(___wrapStr(key, title, url));// 打印URL
		var content = "<iframe name='jeasyuiTabsLoadedCallback:" + key + "' src='" + url + "' style='width: 100%; height: 100%; border: 0;'></iframe>";// 注意引号的解析
		var tabPanelIndex = gainTabPanelIndex(key);
		if (tabPanelIndex > -1) {
			nodeCrud.tabs('select', tabPanelIndex);
			if (______reloadPageOnExist) {
				nodeCrud.tabs('update', {
				    tab : nodeCrud.tabs('getTab', tabPanelIndex),
				    options : {
				        id : key,
				        title : title,
				        content : content,
				        closable : true,
				        selected : true
				    }
				});
			}
		} else {
			nodeCrud.tabs('add', {
			    id : key,
			    title : title,
			    content : content,
			    closable : true,
			    selected : true
			});
		}
	}
}

function gainTabPanelIndex(key) {
	var tabPanelIndex = -1;
	var panelArr = jQuery('#node_crud .tabs-panels .panel');
	for (var i = 0; i < panelArr.length; i++) {
		if (jQuery(panelArr[i]).find('#' + key).length > 0) {
			tabPanelIndex = i;
			break;
		}
	}
	return tabPanelIndex;
}

function jeasyuiTabsLoadedCallback(key) {
	jQuery('#' + key).removeClass('jeasyui-tabs-loading-wrapper');
	jQuery('#' + key + ' .jeasyui-tabs-loading-contents').remove();
}
