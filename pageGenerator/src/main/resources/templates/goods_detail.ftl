<!DOCTYPE HTML>
<html>
<head>
  <title>商品详情</title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <!-- jquery -->
  <script type="text/javascript" src="/js/jquery.min.js"></script>
  <!-- bootstrap -->
  <link rel="stylesheet" type="text/css" href="/bootstrap/css/bootstrap.min.css"/>
  <script type="text/javascript" src="/bootstrap/js/bootstrap.min.js"></script>
  <!-- jquery-validator -->
  <script type="text/javascript" src="/jquery-validation/jquery.validate.min.js"></script>
  <script type="text/javascript" src="/jquery-validation/localization/messages_zh.min.js"></script>
  <!-- layer -->
  <script type="text/javascript" src="/layer/layer.js"></script>
  <!-- md5.js -->
  <script type="text/javascript" src="/js/md5.min.js"></script>
  <!-- common.js -->
  <script type="text/javascript" src="/js/common.js"></script>
  <style type="text/css">
    html, body {
      height: 100%;
      width: 100%;
    }

    body {
      background-size: 100% 100%;
    }

    #goodslist td {
      border-top: 1px solid #39503f61;
    }
  </style>
</head>
<body>

<div class="panel panel-default" style="height:100%;background-color:rgba(222,222,222,0.8)">
  <div class="panel-heading">秒杀商品详情</div>
  <div class="panel-body">
    <span id="userTip"> 您还没有登录，请登陆后再操作<br/></span>
    <span>没有收货地址的提示。。。</span>
  </div>
  <table class="table" id="goodslist">
    <tr>
      <td>商品名称</td>
      <td colspan="3" id="goodsName">${goods.goodsName}</td>
    </tr>
    <tr>
      <td>商品图片</td>
      <td colspan="3"><img src="${goods.goodsImg}" id="goodsImg" width="200" height="200"/></td>
    </tr>
    <tr>
      <td>秒杀开始时间</td>
      <td id="startTime"></td>
      <td>
        <input type="hidden" id="remainSeconds" value="${remainSeconds}"/>
        <span id="miaoshaTip"></span>
      </td>
      <td>

        <div class="row">
          <div class="form-inline">
            <img id="verifyCodeImg" width="80" height="32" style="display:none" onclick="refreshVerifyCode()"/>
            <input id="verifyCode" class="form-control" style="display:none"/>
            <button class="btn btn-primary" type="button" id="buyButton" onclick="getMiaoshaPath()">立即秒杀</button>
          </div>
        </div>
        <input type="hidden" name="goodsId" id="goodsId" value="${goods.id}"/>
      </td>
    </tr>
    <tr>
      <td>商品原价</td>
      <td colspan="3" id="goodsPrice">${goods.goodsPrice}</td>
    </tr>
    <tr>
      <td>秒杀价</td>
      <td colspan="3" id="miaoshaPrice">${goods.miaoshaPrice}</td>
    </tr>
    <tr>
      <td>库存数量</td>
      <td colspan="3" id="stockCount">${goods.stockCount}</td>
    </tr>
  </table>
</div>
</body>
<script>

  function getMiaoshaPath() {
    var goodsId = $("#goodsId").val();
    g_showLoading();
    $.ajax({
      url: "/miaosha/path",
      type: "GET",
      data: {
        goodsId: goodsId,
        verifyCode: $("#verifyCode").val()
      },
      success: function (data) {
        if (data.code == 0) {
          var path = data.data;
          doMiaosha(path);
        } else {
          layer.msg(data.msg);
        }
      },
      error: function () {
        layer.msg("客户端请求有误");
      }
    });
  }

  function getMiaoshaResult(goodsId) {
    g_showLoading();
    $.ajax({
      url: "/miaosha/result",
      type: "GET",
      data: {
        goodsId: $("#goodsId").val(),
      },
      success: function (data) {
        if (data.code == 0) {
          var result = data.data;
          if (result < 0) {
            layer.msg("对不起，秒杀失败");
          } else if (result == 0) {//继续轮询
            setTimeout(function () {
              getMiaoshaResult(goodsId);
            }, 5000);
          } else {
            layer.confirm("恭喜你，秒杀成功！查看订单？", {btn: ["确定", "取消"]},
                    function () {
                      window.location.href = "/order_detail.htm?orderId=" + result;
                    },
                    function () {
                      layer.closeAll();
                    });
          }
        } else {
          layer.msg(data.msg);
        }
      },
      error: function () {
        layer.msg("客户端请求有误");
      }
    });
  }

  function doMiaosha(path) {
    $.ajax({
      url: "/miaosha/" + path + "/do_miaosha",
      type: "POST",
      data: {
        goodsId: $("#goodsId").val()
      },
      success: function (data) {
        if (data.code == 0) {
          //window.location.href="/order_detail.htm?orderId="+data.data.id;
          getMiaoshaResult($("#goodsId").val());
        } else {
          layer.msg(data.msg);
        }
      },
      error: function () {
        layer.msg("客户端请求有误");
      }
    });

  }
// ---------------------------------------------------------------------------



  $(function () {
    countDown();
  });



  function countDown() {
    var remainSeconds = $("#remainSeconds").val();
    var timeout;
    if (remainSeconds > 0) {//秒杀还没开始，倒计时
      $("#buyButton").attr("disabled", true);
      $("#miaoshaTip").html("秒杀倒计时：" + remainSeconds + "秒");
      timeout = setTimeout(function () {
        $("#countDown").text(remainSeconds - 1);
        $("#remainSeconds").val(remainSeconds - 1);
        countDown();
      }, 1000);
    } else if (remainSeconds == 0) {//秒杀进行中
      $("#buyButton").attr("disabled", false);
      if (timeout) {
        clearTimeout(timeout);
      }
      $("#miaoshaTip").html("秒杀进行中");
      $("#verifyCodeImg").attr("src", "/miaosha/verifyCode?goodsId=" + $("#goodsId").val());
      $("#verifyCodeImg").show();
      $("#verifyCode").show();
    } else {//秒杀已经结束
      $("#buyButton").attr("disabled", true);
      $("#miaoshaTip").html("秒杀已经结束");
      $("#verifyCodeImg").hide();
      $("#verifyCode").hide();
    }
  }
  function refreshVerifyCode() {
    $("#verifyCodeImg").attr("src", "/miaosha/verifyCode?goodsId=" + $("#goodsId").val() + "&timestamp=" + new Date().getTime());
  }
</script>
</html>
