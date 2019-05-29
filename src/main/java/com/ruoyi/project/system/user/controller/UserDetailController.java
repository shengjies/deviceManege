package com.ruoyi.project.system.user.controller;

import com.ruoyi.framework.config.RuoYiConfig;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.project.device.devCompany.domain.DevCompany;
import com.ruoyi.project.device.devCompany.service.IDevCompanyService;
import com.ruoyi.project.system.menu.domain.Menu;
import com.ruoyi.project.system.menu.service.IMenuService;
import com.ruoyi.project.system.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 用户个人设置控制层
 */
@Controller
@RequestMapping("/userDetail")
public class UserDetailController extends BaseController {

    @Autowired
    private IMenuService menuService;

    @Autowired
    private RuoYiConfig ruoYiConfig;

    @Autowired
    private IDevCompanyService devCompanyService;

    private String prefix = "system/userDetail";

    @GetMapping("/userDetail")
    public String userDetail(ModelMap mmap){
        // 取身份信息
        User user = getSysUser();
        // 根据用户id取出菜单
        List<Menu> menus = menuService.selectMenusByUser(user);
        // 根据用户id查询出公司信息
        DevCompany devCompany = devCompanyService.selectDevCompanyById(user.getCompanyId());
        user.setDevCompany(devCompany);
        mmap.put("menus", menus);
        mmap.put("user", user);
        mmap.put("copyrightYear", ruoYiConfig.getCopyrightYear());
        return prefix +"/userDetail";
    }

}

