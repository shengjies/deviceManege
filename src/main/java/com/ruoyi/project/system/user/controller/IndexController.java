package com.ruoyi.project.system.user.controller;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.security.ShiroUtils;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.device.devCompany.domain.DevCompany;
import com.ruoyi.project.device.devCompany.service.DevCompanyServiceImpl;
import com.ruoyi.project.device.devCompany.service.IDevCompanyService;
import com.ruoyi.project.system.user.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import com.ruoyi.framework.config.RuoYiConfig;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.project.system.menu.domain.Menu;
import com.ruoyi.project.system.menu.service.IMenuService;
import com.ruoyi.project.system.user.domain.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 首页 业务处理
 *
 * @author ruoyi
 */
@Controller
public class IndexController extends BaseController {
    @Autowired
    private IMenuService menuService;

    @Autowired
    private RuoYiConfig ruoYiConfig;

    @Autowired
    private IDevCompanyService devCompanyService;

    @Autowired
    private IUserService userService;

    // 系统首页
    @GetMapping("/index")
    public String index(ModelMap mmap) {
        // 取身份信息
        User user = getSysUser();
        // 根据用户id取出菜单
        List<Menu> menus = menuService.selectMenusByUser(user);
        DevCompany company = devCompanyService.selectDevCompanyById(user.getCompanyId());
        user.setDevCompany(company);

        mmap.put("menus", menus);
        mmap.put("user", user);
        mmap.put("copyrightYear", ruoYiConfig.getCopyrightYear());
        return "index";
    }

    // 系统介绍
    @GetMapping("/system/main")
    public String main(ModelMap mmap) {
        // 取身份信息
        User user = getSysUser();
        // 根据用户id取出菜单
        List<Menu> menus = menuService.selectMenusByUser(user);
        DevCompany devCompany = devCompanyService.selectDevCompanyById(user.getCompanyId());
        user.setDevCompany(devCompany);
        if(devCompany != null && !StringUtils.isEmpty(devCompany.getComPicture())){
            mmap.put("comPictures", JSON.parseArray(devCompany.getComPicture()));
        }else {
            mmap.put("comPictures", null);
        }
        mmap.put("user", user);
        mmap.put("version", ruoYiConfig.getVersion());

        return "main";
    }

    /**
     * 验证用户的登录标记
     *
     * @return
     */
    @PostMapping("/checkUserLoginTag")
    @ResponseBody
    public AjaxResult checkUserLoginTag() {
        User sysUser = userService.selectUserById(ShiroUtils.getUserId());
        if (sysUser.getLoginTag().equals(UserConstants.LOGIN_TAG_ADD)) { // 说明用户已经设置过了
            return AjaxResult.success("success", "1");
        } else {
            return AjaxResult.success("error", "0"); // 用户未设置
        }
    }
}
