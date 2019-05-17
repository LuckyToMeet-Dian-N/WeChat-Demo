package com.gentle.controller;

import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayRefundNotifyResult;
import com.github.binarywang.wxpay.bean.request.WxPayRefundRequest;
import com.github.binarywang.wxpay.bean.result.WxPayRefundResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : Gentle
 * @date : 2019/5/17 12:38
 * @description:
 */
@RestController
@RequestMapping(value = "/api/client/refund/")
@Slf4j
public class WeChatRefundController {

    private static final String REFUND_SUCCESS = "SUCCESS";

    @Autowired
    private WxPayService wxPayService;

    @PostMapping(value = "weChatRefund")
    public String refund() {
        //申请退款
        WxPayRefundRequest refundInfo = WxPayRefundRequest.newBuilder()
                //订单号
                .outTradeNo("自己系统订单号")
                //退款订单号
                .outRefundNo("自己系统订单号")
                //金额
                .totalFee(yuanToFee(new BigDecimal(100)))
                //退款金额
                .refundFee(yuanToFee(new BigDecimal(100)))
                //todo 回调地址
                .notifyUrl("http://我们系统的域名/api/client/refund/refundNotify")
                .build();
        WxPayRefundResult wxPayRefundResult;
        try {
            wxPayRefundResult = wxPayService.refund(refundInfo);
            //判断退款信息是否正确
            if (REFUND_SUCCESS.equals(wxPayRefundResult.getReturnCode())
                    && REFUND_SUCCESS.equals(wxPayRefundResult.getResultCode())) {
                /**
                 * 系统内部业务逻辑
                 */
                return "正在退款中。。";
            }
        } catch (WxPayException e) {
            log.error("微信退款接口错误信息= {}", e);
        }

        return "退款失败";
    }


    /**
     * 仅支持一次性退款，多次退款需要修改逻辑
     * @param xmlData 微信返回的流数据
     * @return
     */
    @RequestMapping(value = "refundNotify",method = {RequestMethod.GET,RequestMethod.POST})
    public String refundNotify(@RequestBody String xmlData) {

        WxPayRefundNotifyResult wxPayRefundNotifyResult;
        try {
            wxPayRefundNotifyResult = wxPayService.parseRefundNotifyResult(xmlData);
        } catch (WxPayException e) {
            log.error("退款失败，失败信息:{}", e);
            return WxPayNotifyResponse.fail("退款失败");
        }
        //判断你返回状态信息是否正确
        if (REFUND_SUCCESS.equals(wxPayRefundNotifyResult.getReturnCode())) {
            WxPayRefundNotifyResult.ReqInfo reqInfo = wxPayRefundNotifyResult.getReqInfo();
            //判断退款状态
            if (REFUND_SUCCESS.equals(reqInfo.getRefundStatus())) {
                //内部订单号
                String outTradeNo = reqInfo.getOutTradeNo();
                /**
                 * 一、可能会重复回调，需要做防重判断
                 * 二、处理我们系统内部业务，做修改订单状态，释放资源等！
                 */
                return WxPayNotifyResponse.success("退款成功！");
            }
        }

        return WxPayNotifyResponse.fail("回调有误!");
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