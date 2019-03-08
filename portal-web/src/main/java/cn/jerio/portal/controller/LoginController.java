package cn.jerio.portal.controller;

import cn.jerio.result.Result;
import cn.jerio.user.service.MiaoShaUserService;
import cn.jerio.vo.LoginVo;
import com.alibaba.dubbo.config.annotation.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * Created by Jerio on 2019/03/01
 */
@Controller
@RequestMapping("/login")
public class LoginController {

    private static Logger log = LoggerFactory.getLogger(LoginController.class);

    @Reference
    private MiaoShaUserService userService;

    @RequestMapping("/to_login")
    public String toLogin() {
        return "login";
    }

    @RequestMapping("/do_login")
    @ResponseBody
    public Result<String> doLogin(HttpServletResponse response, @Valid LoginVo loginVo) {
        String token = userService.login(loginVo);
        addCookie(response,token);
        return Result.success(token);
    }

    private void addCookie(HttpServletResponse response,String token){
        Cookie cookie = new Cookie("token",token);
        cookie.setMaxAge(24*60*60);
        cookie.setDomain("miaosha.com");
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
