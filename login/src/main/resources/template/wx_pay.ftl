

<html>
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>微信支付</title>
    <script type="text/javascript" src="https://res.wx.qq.com/open/js/jweixin-1.6.0.js"></script>
    <style>
        body{font-family:"Microsoft YaHei",Helvetica,"Hiragino Sans GB",Arial,sans-serif;font-size:24px;line-height:40px;color:#737373;background-color:#fff;margin:50px 30px 0}table{margin:10px 0 15px 0;border-collapse:collapse}td,th{border:1px solid #ddd;padding:3px 10px}
        .ex{color:#ccc}
        .btn{color:white;height:60px;width:100%;font-size:26px;background-color:#07c160;border-radius:15px;border:0px}
    </style>
</head>
<body>

<script type="text/javascript">

    function pay(){
        // config信息验证后会执行 ready 方法，所有接口调用都必须在 config 接口获得结果之后，config是一个客户端的异步操作，所以如果需要在页面加载时就调用相关接口，则须把相关接口放在 ready 函数中调用来确保正确执行。对于用户触发时才调用的接口，则可以直接调用，不需要放在 ready 函数中。
        wx.chooseWXPay({
            timestamp:'${timestamp}', // 支付签名时间戳，注意微信 jssdk 中的所有使用 timestamp 字段均为小写。但最新版的支付后台生成签名使用的 timeStamp 字段名需大写其中的 S 字符
            nonceStr: '${nonceStr}', // 支付签名随机串，不长于 32 位
            package: 'prepay_id=${prepay_id}', // 统一支付接口返回的prepay_id参数值，提交格式如：prepay_id=\*\*\*）
            signType: 'RSA', // 微信支付V3的传入 RSA ,微信支付V2的传入格式与V2统一下单的签名格式保持一致
            paySign: '${paySign}', // 支付签名
            success: function (res) {
                console.log(res);
                wx.closeWindow();

            },
            fail: function (res){
                console.log(res);
                wx.closeWindow();
            },
            cancel: function (res){
                console.log(res);
                wx.closeWindow();
            }
        });
    }

    wx.config({
        debug: false, // 开启调试模式,调用的所有 api 的返回值会在客户端 alert 出来，若要查看传入的参数，可以在 pc 端打开，参数信息会通过 log 打出，仅在 pc 端时才会打印。
        appId: '${appId}', // 必填，公众号的唯一标识
        timestamp: '${timestamp}', // 必填，生成签名的时间戳
        nonceStr: '${nonceStr}', // 必填，生成签名的随机串
        signature: '${signature}',// 必填，签名
        jsApiList: ['chooseWXPay'] // 必填，需要使用的 JS 接口列表
    });

    wx.ready(pay)

    wx.error(function(res){
        console.log(res);
        wx.closeWindow();
    });

</script>

<p>游戏： 契约学院</p>

<p>金额： ${price} 元</p>

<#--<p>物品：  ${itemName} </p>-->
<p></p>
<p></p>
<p>自动拉起支付，如未自动跳转，请手动选择确认支付</p>

<p></p>
<p></p>
<button class="btn" onclick="pay()" type="primary">确认支付</button>
<p class="ex">*温馨提示：如无法拉起支付，请在客服会话中联系客服</p>

</body>

</html>
