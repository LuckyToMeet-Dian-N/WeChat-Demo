package com.gentle.controller;

import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author : Gentle
 * @date : 2019/5/17 10:47
 * @description:
 */
@Controller
@RequestMapping(value = "/api/client/")
public class WeChatLoginController {
    @Autowired
    private WxMpService wxMpService;

    /**
     * 微信授权入口，请求该接口
     * @return
     */
    @GetMapping(value = "weChatLogin")
    public String weChatRedirect() {
        String url = "http://修改成自己的域名/api/client/apiTest";
        String redirectURL = wxMpService.oauth2buildAuthorizationUrl(url, WxConsts.OAUTH2_SCOPE_USER_INFO, null);

        return "redirect:" + redirectURL;
    }

    /**
     * 微信重定向回来，并携带 code　参数
     *
     * @param code 微信返回的 code
     * @return
     */
    @GetMapping(value = "apiTest")
    public String redirectToIndexPage(@RequestParam("code") String code) {

        //请求微信，拿到微信信息
        try {
            //根据 code 换取 accessToken
            WxMpOAuth2AccessToken wxMpOAuth2AccessToken = wxMpService.oauth2getAccessToken(code);
            /**
             * 这里处理内部业务，如判断数据库中是否已经有该用户了
             */
            //换取用户信息
            WxMpUser wxMpUser = wxMpService.oauth2getUserInfo(wxMpOAuth2AccessToken, null);

            /**
             * 消息入库等等之类的代码
             */
            System.out.println("得到授权的个人信息 ：" + wxMpUser);
        } catch (WxErrorException e) {
            e.printStackTrace();

        }
        //这里可以做重定向到某个页面，懒得做页面就算了。
        return "请求成功！";
    }


}