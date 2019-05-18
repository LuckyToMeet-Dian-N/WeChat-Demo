package com.gentle.controller;


import com.gentle.config.WeChatPayProperties;
import com.gentle.entity.ReturnPayInfoVO;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.util.SignUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : Gentle
 * @date : 2019/5/17 12:11
 * @description:
 */
@RestController
@RequestMapping(value = "/api/client/pay/")
public class WeChatPayController {

    @Autowired
    private WxPayService wxPayService;
    @Autowired
    WeChatPayProperties weChatPayProperties;

    /**
     * 此处处理订单信息，构建订单数据。
     *
     * 将构建好的支付参数返回到前端，前端调起微信支付
     * @return
     */
    @GetMapping(value = "weChatPay")
    public ReturnPayInfoVO weChatPay() {
        /**
         * 处理内部业务，校验订单等
         */
        final WxPayUnifiedOrderRequest wxPayUnifiedOrderRequest = WxPayUnifiedOrderRequest.newBuilder()
                //调起支付的人的 openId
                .openid("openId")
                //订单编号
                .outTradeNo("我们系统内部订单号")
                //订单金额
                .totalFee(yuanToFee(new BigDecimal(100)))
                //商品描述
                .body("订单信息")
                //获取本地IP
                .spbillCreateIp(InetAddress.getLoopbackAddress().getHostAddress())
                //回调的 URL 地址
                .notifyUrl("http://我们的域名/api/client/pay/weChatPayNotify")
                .build();
        WxPayUnifiedOrderResult wxPayUnifiedOrderResult =null;
        try {
            wxPayUnifiedOrderResult = wxPayService.unifiedOrder(wxPayUnifiedOrderRequest);
        } catch (WxPayException e) {
            e.printStackTrace();
            throw new RuntimeException("微信支付调起失败！");
        }
        //组合参数构建支付
        Map<String, String> paySignInfo = new HashMap<>(5);
        String timeStamp = createTimestamp();
        String nonceStr = String.valueOf(System.currentTimeMillis());
        paySignInfo.put("appId", weChatPayProperties.getAppId());
        paySignInfo.put("nonceStr", nonceStr);
        paySignInfo.put("timeStamp", timeStamp);
        paySignInfo.put("signType", "MD5");
        paySignInfo.put("package", "prepay_id=" + wxPayUnifiedOrderResult.getPrepayId());
        String paySign = SignUtils.createSign(paySignInfo, "MD5", weChatPayProperties.getMchKey(), false);

        //组合支付参数
        ReturnPayInfoVO returnPayInfoVO = new ReturnPayInfoVO();
        returnPayInfoVO.setAppId(weChatPayProperties.getAppId());
        returnPayInfoVO.setNonceStr(nonceStr);
        returnPayInfoVO.setPaySign(paySign);
        returnPayInfoVO.setSignType("MD5");
        returnPayInfoVO.setPrepayId(wxPayUnifiedOrderResult.getPrepayId());
        returnPayInfoVO.setTimeStamp(timeStamp);

        return returnPayInfoVO;
    }

    /**
     *
     * @param xmlData 微信返回的流
     * @return
     */
    @RequestMapping(value = "weChatPayNotify",method = {RequestMethod.GET,RequestMethod.POST})
    public String weChatNotify(@RequestBody String xmlData){

        try {
            final WxPayOrderNotifyResult notifyResult = this.wxPayService.parseOrderNotifyResult(xmlData);
            //这里是存储我们发起支付时订单号的信息，所以取出来
            notifyResult.getOutTradeNo();
            /**
             * 系统内部业务，修改订单状态之类的
             */
            //成功后回调微信信息
            return WxPayNotifyResponse.success("回调成功！");
        } catch (WxPayException e) {
            e.printStackTrace();
            return WxPayNotifyResponse.fail("回调有误!");
        }
    }

    /**
     * 1 块钱转为 100 分
     * 元转分
     *
     * @param bigDecimal 钱数目
     * @return 分
     */
    private int yuanToFee(BigDecimal bigDecimal) {
        return bigDecimal.multiply(new BigDecimal(100)).intValue();
    }

    /**
     * 时间
     *
     * @return 时间戳
     */
    private String createTimestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }

}