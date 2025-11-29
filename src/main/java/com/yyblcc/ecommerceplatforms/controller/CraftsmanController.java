package com.yyblcc.ecommerceplatforms.controller;

import com.yyblcc.ecommerceplatforms.annotation.UpdateBloomFilter;
import com.yyblcc.ecommerceplatforms.domain.DTO.*;
import com.yyblcc.ecommerceplatforms.domain.DTO.CraftsmanAuthDTO;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.CraftsmanQuery;
import com.yyblcc.ecommerceplatforms.service.CraftsmanAuthService;
import com.yyblcc.ecommerceplatforms.service.CraftsmanService;
import com.yyblcc.ecommerceplatforms.util.StpKit;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/craftsman")
@Slf4j
@RequiredArgsConstructor
public class CraftsmanController {


    private final CraftsmanService craftsmanService;
    private final CraftsmanAuthService craftsmanAuthService;

    /**
     * 匠人注册
     * @param craftsmanDTO
     * @return
     */
    @PostMapping("/save")
    @UpdateBloomFilter
    public Result<?> save(@RequestBody CraftsmanDTO craftsmanDTO) {
        log.info("craftsmanRegisterDTO={}", craftsmanDTO);
        return craftsmanService.saveCraftsman(craftsmanDTO);
    }

    @GetMapping("/check")
    public Result<?> check(@RequestParam String username,@RequestParam String phone) {
        log.info("username={},phone={}", username, phone);
        return craftsmanService.checkCraftsmanInfo(username,phone);
    }

    @GetMapping("/check-email")
    public Result<?> checkEmail(@RequestParam String email) {
        log.info("userInfoCheckDTO={}", email);
        return craftsmanService.checkEmail(email);
    }

    /**
     * 修改匠人账号状态
     * @param status
     * @param id
     * @return
     */
    @PutMapping("/status")
    public Result<?> changeStatus(@RequestParam("status")Integer status, @RequestParam("id")Long id) {
        log.info("status={},id={}", status, id);
        return craftsmanService.updateCraftsmanStatus(status,id);
    }

    /**
     * 修改匠人认证状态
     * @param reviewStatus
     * @param id
     * @return
     */
    @PutMapping("/review-status")
    public Result<?> setReviewStatus(@RequestParam("reviewStatus")Integer reviewStatus, @RequestParam("id")Long id) {
        log.info("status={},id={}", reviewStatus, id);
        return craftsmanService.updateCraftsmanReviewStatus(reviewStatus,id);
    }

    /**
     * 匠人修改个人信息
     * @param craftsmanDTO
     * @param request
     * @return
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody CraftsmanDTO craftsmanDTO, HttpServletRequest request) {
        log.info("adminDTO={}", craftsmanDTO);
        return craftsmanService.updateCraftsman(craftsmanDTO,request);
    }

    /**
     * 匠人修改密码
     * @param passwordDTO
     * @return
     */
    @PutMapping("/update-password")
    public Result<?> updatePassword(@RequestBody PasswordDTO passwordDTO,HttpServletRequest request) {
        log.info("passwordDTO={}", passwordDTO);
        return craftsmanService.updatePassword(passwordDTO,request);
    }

    @DeleteMapping("/delete")
    public Result<?> delete(@RequestParam("id")Long id) {
        log.info("delete={}", id);
        return craftsmanService.remove(id);
    }

    @DeleteMapping("/batchdelete")
    public Result<?> batchDelete(@RequestParam("ids") List<Long> ids) {
        log.info("batchDelete={}", ids);
        return craftsmanService.batchRemove(ids);
    }

    @GetMapping
    public Result<?> select(){
        return craftsmanService.getCraftsman();
    }

    @GetMapping("/page")
    public Result<PageBean> page(CraftsmanQuery craftsmanQuery){
        log.info("craftsmanQuery={}", craftsmanQuery);
        return craftsmanService.page(craftsmanQuery);
    }

    @GetMapping("/profile/{craftsmanId}")
    public Result<?> getProfile(@PathVariable("craftsmanId")Long craftsmanId){
        log.info("profile={}", craftsmanId);
        return craftsmanService.getProfile(craftsmanId);
    }

    @PostMapping("/login")
    public Result login(@RequestBody LoginDTO loginDTO, HttpServletRequest request){
        log.info("login={}", loginDTO);
        return craftsmanService.login(loginDTO,request);
    }

    @PostMapping("/logout")
    public Result logout(){
        Long craftsmanId = StpKit.CRAFTSMAN.getLoginIdAsLong();
        log.info("craftsman {} 登出", craftsmanId);
        StpKit.CRAFTSMAN.logout(craftsmanId);
        return Result.success("已退出登录");
    }

    @PutMapping("/resetPassword")
    public Result resetPassword(@RequestParam("craftsmanId") Long craftsmanId){
        log.info("craftsmanId={}", craftsmanId);
        return craftsmanService.resetPassword(craftsmanId);
    }

    @GetMapping("/nameselect")
    public Result nameSelect(@Validated CraftsmanQuery craftsmanQuery){
        log.info("craftsmanQuery={}", craftsmanQuery);
        return craftsmanService.nameSelect(craftsmanQuery);
    }

    @PostMapping("/signup-auth")
    public Result signUpAuth(@RequestBody @Validated CraftsmanAuthDTO craftsmanAuthDTO){
        log.info("craftsmanAuth={}", craftsmanAuthDTO);
        return craftsmanService.signUpAuth(craftsmanAuthDTO);
    }

    @GetMapping("/page-review")
    public Result pageReview(@RequestParam Integer page, @RequestParam Integer pageSize){
        log.info("page={},pageSize={}", page, pageSize);
        return craftsmanAuthService.pageReview(page,pageSize);
    }
}
