package com.yyblcc.ecommerceplatforms.controller.admin;

import com.yyblcc.ecommerceplatforms.annotation.UpdateBloomFilter;
import com.yyblcc.ecommerceplatforms.domain.DTO.AdminDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.LoginDTO;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 添加新的管理员
     * @param adminDTO
     * @return
     */
    @PostMapping("/save")
    @UpdateBloomFilter
    public Result<?> save(@RequestBody AdminDTO adminDTO) {
        log.info("adminRegisterDTO={}", adminDTO);
        return adminService.saveAdmin(adminDTO);
    }

    /**
     * 修改管理员账号状态
     * @param status
     * @param id
     * @return
     */
    @PutMapping("/status")
    public Result<?> changeStatus(@RequestParam("status")Integer status, @RequestParam("id")Long id) {
        log.info("status={},id={}", status, id);
        return adminService.updateAdminStatus(status,id);
    }

    /**
     * 管理员更新个人信息
     * @param adminDTO
     * @return
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody AdminDTO adminDTO) {
        log.info("adminDTO={}", adminDTO);
        return adminService.updateAdmin(adminDTO);
    }

    /**
     * 删除管理员，只有超级管理员有权限
     * @param id
     * @return
     */
    @DeleteMapping("/delete")
    public Result<?> delete(@RequestParam("id")Long id) {
        log.info("delete={}", id);
        return adminService.remove(id);
    }

    /**
     * 批量删除管理员，只有超级管理员有权限
     * @param ids
     * @return
     */
    @DeleteMapping("/batchdelete")
    public Result<?> batchDelete(@RequestParam("ids") List<Long> ids) {
        log.info("batchDelete={}", ids);
        return adminService.batchRemove(ids);
    }

    /**
     * 查询所有管理员
     * @return
     */
    @GetMapping
    public Result<?> select(){
        return adminService.getAdmins();
    }

    /**
     * 分页查询管理员
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public Result<PageBean> page(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "5")Integer pageSize){
        log.info("page={},pageSize={}", page, pageSize);
        return adminService.page(page,pageSize);
    }

    /**
     * 管理员登录
     * @param loginDTO
     * @param request
     * @return
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginDTO loginDTO, HttpServletRequest request){
        log.info("login={}", loginDTO);
        return adminService.login(loginDTO,request);
    }

    @PutMapping("/resetPassword")
    public Result resetPassword(@RequestParam("adminId") Long adminId){
        log.info("adminId={}", adminId);
        return adminService.resetPassword(adminId);
    }

    @GetMapping("/nameselect")
    public Result nameselect(@RequestParam String name,@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "1") Integer pageSize){
        log.info("name= {},page={},pageSize={}",name, page, pageSize);
        return adminService.getAdminByName(name,page,pageSize);
    }

}
