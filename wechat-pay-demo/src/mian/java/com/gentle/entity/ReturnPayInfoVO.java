package com.gentle.entity;

import lombok.Data;

/**
 * @author : Gentle
 * @date : 2019/5/5 20:07
 * @description: 返回交给前端调起支付的信息
 */
@Data
public class ReturnPayInfoVO {
    /**
     * appid
     */
    private String appId;

    private String timeStamp;
    private String nonceStr;
    private String prepayId;
    private String paySign;
    private String signType;

}