//===================菜单模块开始===================
var menuInfoJsonArray = null;
var menuInfoHtml = '';
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
var tabKeyIndex = 0;
var tablKeyPrefix = 'jeasyui_Tabs__';
// ===================jeasyui tabs结束===================

// ===================通行证数据开始===================
var ______userInfoId = null;// 用户id
var ______userInfoIdMD5 = null;// 用户idMD5
var ______reloadPageOnExist = false;// reload_page_on_exist
var ______askOnCloseLabel = false;// ask_on_close_label
var ______remoteClock = new Date();// 遠程時鐘
var ______requestRemoteClockBegin = 0;// 請求遠程時鐘開始毫秒數
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

	var disposeLeaf = function(leaf) {
		if (leaf.length > 0) {
			for (var i = 0; i < leaf.length; i++) {
				var node = leaf[i];
				menuInfoHtml += '<li>';
				var iconSrc = node['iconSrc'];
				if (null == iconSrc || '' == iconSrc) {
					iconSrc = '../images/icon_plain_folder_16x16.png';
				}
				menuInfoHtml += '<img class="function-icon" src="' + iconSrc + '">';
				menuInfoHtml += '<span class="click"';
				if (0 == node['leaf'].length) {
					menuInfoHtml += ' tab-key="' + md5(String(tabKeyIndex++)) + '"';
					if (node['tabUrl'] != null && node['tabUrl'] != '') {
						menuInfoHtml += ' tab-url="' + node['tabUrl'] + '"';
					}
					if (node['tabTitle'] != null && node['tabTitle'] != '') {
						menuInfoHtml += ' tab-title="' + node['tabTitle'] + '"';
					}
					if (node['theValue'] != null && node['theValue'] != '') {
						menuInfoHtml += ' the-value="' + node['theValue'] + '"';
					}
				}
				menuInfoHtml += '>' + node['theValue'] + '</span>';
				if (0 == node['leaf'].length) {
					if (null == node['tabUrl'] || '' == node['tabUrl']) {
						menuInfoHtml += '<img class="message-icon" src="../images/icon_exclamation_mark_16x16.png" title="该功能处于停用状态！">';
					}
				}
				if (node['leaf'].length > 0) {
					menuInfoHtml += '<img class="switch-icon" src="../images/icon_arrow_carrot_right_16x16.png">';
					menuInfoHtml += '<ul style="display: none;">';
					disposeLeaf(node['leaf']);
					menuInfoHtml += '</ul>';
				}
				menuInfoHtml += '</li>';
			}
		}
	};
	if (menuInfoJsonArray != null) {
		for (var i = 0; i < menuInfoJsonArray.length; i++) {
			var node = menuInfoJsonArray[i];
			menuInfoHtml += '<li>';
			var iconSrc = node['iconSrc'];
			if (null == iconSrc || '' == iconSrc) {
				iconSrc = '../images/icon_plain_folder_16x16.png';
			}
			menuInfoHtml += '<img class="function-icon" src="' + iconSrc + '">';
			menuInfoHtml += '<span class="click"';
			if (0 == node['leaf'].length) {
				menuInfoHtml += ' tab-key="' + md5(String(tabKeyIndex++)) + '"';
				if (node['tabUrl'] != null && node['tabUrl'] != '') {
					menuInfoHtml += ' tab-url="' + node['tabUrl'] + '"';
				}
				if (node['tabTitle'] != null && node['tabTitle'] != '') {
					menuInfoHtml += ' tab-title="' + node['tabTitle'] + '"';
				}
				if (node['theValue'] != null && node['theValue'] != '') {
					menuInfoHtml += ' the-value="' + node['theValue'] + '"';
				}
			}
			menuInfoHtml += '>' + node['theValue'] + '</span>';
			if (0 == node['leaf'].length) {
				if (null == node['tabUrl'] || '' == node['tabUrl']) {
					menuInfoHtml += '<img class="message-icon" src="../images/icon_exclamation_mark_16x16.png" title="该功能处于停用状态！">';
				}
			}
			if (node['leaf'].length > 0) {
				menuInfoHtml += '<img class="switch-icon" src="../images/icon_arrow_carrot_right_16x16.png">';
				menuInfoHtml += '<ul style="display: none;">';
				disposeLeaf(node['leaf']);
				menuInfoHtml += '</ul>';
			}
			menuInfoHtml += '</li>';
		}
	}
	jQuery('.div-main-left .node_root').html(menuInfoHtml);

	jQuery('.div-main-left .click').click(function() {
		var nextUl = jQuery(this).nextAll('ul');
		var switchIcon = jQuery(this).nextAll('.switch-icon');
		if (nextUl.length > 0) {
			if (!jQuery(nextUl).is(':visible')) {
				switchIcon.attr('src', '../images/icon_arrow_carrot_down_16x16.png');
			} else {
				switchIcon.attr('src', '../images/icon_arrow_carrot_right_16x16.png');
			}
			jQuery('.div-main-left .mask-layer').show();
			nextUl.toggle('fast', function() {
				jQuery('.div-main-left .mask-layer').hide();
				jQuery('.div-main-left').getNiceScroll().resize();
			});
		} else {
			var tabKey = jQuery(this).attr('tab-key');
			var tabUrl = jQuery(this).attr('tab-url');
			var tabTitle = jQuery(this).attr('tab-title');
			var theValue = jQuery(this).attr('the-value');
			if (tabUrl != undefined && tabUrl != null && tabUrl != '') {
				jQuery('.div-main-left .mask-layer').show();
				if (tabTitle != undefined && tabTitle != null && tabTitle != '') {
					addTabPanel(tabKey, ___abbr(tabTitle, 10), tabUrl);
				} else {
					addTabPanel(tabKey, ___abbr(theValue, 10), tabUrl);
				}
				setTimeout(function() {
					jQuery('.div-main-left .mask-layer').hide();
					jQuery('.div-main-left').getNiceScroll().resize();
				}, 100);// 目的是减轻服务器压力，防止恶意攻击
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
				return false;
			}
		}
	});

	jQuery('img.switch-icon').click(function() {
		jQuery(this).prev('.click').click();
	});

	var nodeCrud = jQuery('#node_crud');
	nodeCrud.tabs({
	    onSelect : function(title, index) {
		    var tabKey = nodeCrud.tabs('getTab', index)[0].id.substring(tablKeyPrefix.length);
		    jQuery('.div-main-left .click[tab-key!="' + tabKey + '"]').removeClass('this-menu-highlight');
		    jQuery('.div-main-left .click[tab-key="' + tabKey + '"]').addClass('this-menu-highlight');
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
			    jQuery('.div-main-left .click.this-menu-highlight').removeClass('this-menu-highlight');
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
	jQuery('.div-main-left').getNiceScroll().resize();
	jQuery('.div-main-left').getNiceScroll(0).doScrollLeft(0, 0);
	jQuery('.div-main-left').getNiceScroll(0).doScrollTop(0, 0);
}

function requestServerInfoServlet() {
	______requestRemoteClockBegin = new Date().getTime();// 設置請求遠程時鐘開始時間毫秒數
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
					    requestServerInfoServlet();// 為避免受CPU影響而造成誤差，故在一個周期之外再次發起請求。
				    } else {
					    ______remoteClock.setTime(time);
					    jQuery('#remoteClock').html(______remoteClock.format());
					    timeout_serverInfo = setTimeout('______requestRemoteClockFunction(' + (time + 1000) + ')', 1000);// 間隔一個周期重新發起請求
				    }
			    };
			    var delayed = new Date().getTime() - ______requestRemoteClockBegin;
			    ______requestRemoteClockFunction(Number(result) + delayed);// 計入請求延誤時間
		    } else {
			    jQuery('#remoteClock').html('加載失敗！');
		    }
	    },
	    error : function(err) {
		    jQuery('#remoteClock').html('加載失敗！');
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
		var content = "<iframe src='" + url + "' style='width: 100%; height: 100%; border: 0;' />";
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
