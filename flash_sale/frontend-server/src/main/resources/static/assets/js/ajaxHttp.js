//  通用的ajax方法
function ajaxHttp(obj, callback, err) {
    $.ajax("http://localhost:9000" + obj.url, {
        type: obj.type || 'get',
        contentType: obj.contentType || 'application/json;charset=UTF-8',
        headers: { 'token': localStorage.getItem('token') },
        data: obj.data || {},
        success: function (res) {
            console.log(res);
            // var obj=JSON.parse(res);
            // console.log(obj);
            // console.log(obj.code);
            //  callback(obj);
            if (res.code === 200) {
                //console.log(obj.code);
                callback(res)
            } else {
                if (res.code === -2 || res.code === -3) {
                    localStorage.removeItem('token')
                    $('.commodity-header').find('.seckill-shopping').css('display', 'block')
                    setTimeout(function () {
                        layer.confirm('请先进行登录！！', {
                            btn: ['马上登录', '取消'] //按钮
                        }, function () {
                            window.location.href = '/login.html'
                        }, function (index) {
                            layer.close(index)
                        });
                    }, 200)
                } else {
                    layer.msg(res.msg)//系统异常
                }
            }
        },
        error: err || defaultError
    })
}
function defaultError() {
    layer.msg('网站繁忙，稍后再试！');
}