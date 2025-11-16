package com.yyblcc.ecommerceplatforms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yyblcc.ecommerceplatforms.domain.DTO.AdminDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.LoginDTO;
import com.yyblcc.ecommerceplatforms.domain.po.Admin;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface AdminService extends IService<Admin> {
    Result<?> saveAdmin(AdminDTO adminDTO);

    Result<?> updateAdminStatus(Integer status,Long id);

    Result<?> updateAdmin(AdminDTO adminDTO);

    Result<?> remove(Long id);

    Result<?> batchRemove(List<Long> ids);

    Result<?> getAdmins();

    Result<PageBean> page(Integer page, Integer pageSize);

    Result login(LoginDTO loginDTO, HttpServletRequest request);

    Result resetPassword(Long adminId);

    Result getAdminByName(String name, Integer page, Integer pageSize);

    Result pageReview(Integer page, Integer pageSize);
}
