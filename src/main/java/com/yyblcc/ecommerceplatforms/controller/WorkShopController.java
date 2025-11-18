package com.yyblcc.ecommerceplatforms.controller;

import com.yyblcc.ecommerceplatforms.domain.DTO.WorkShopDTO;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.service.WorkShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/workshop")
@Slf4j
@RequiredArgsConstructor
public class WorkShopController {

    private final WorkShopService workShopService;

    /**
     * 修改店铺认证状态
     * @param workshopId
     * @param status
     * @return
     */
    @PutMapping("/review-workshop")
    public Result reviewWorkshop(@RequestParam("workshopId") Long workshopId, @RequestParam("status") Integer status){
        log.info("workshopId={},status={}", workshopId, status);
        return workShopService.reviewWorkshop(workshopId,status);
    }

    /**
     * 关闭工作室,只有出现违规才可封禁工作室！
     * @param workshopId
     * @return
     */
    @PutMapping("/ban-workshop")
    public Result banWorkshop(@RequestParam("workshopId") Long workshopId){
        log.info("workshopId={}", workshopId);
        return workShopService.banWorkshop(workshopId);
    }

    /**
     * 分页查询工作室，管理员端分页展示
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public Result page(@RequestParam(defaultValue = "1")Integer page, @RequestParam(defaultValue = "5")Integer pageSize){
        log.info("page={},pageSize={}", page, pageSize);
        return workShopService.pageWorkShop(page,pageSize);
    }

    /**
     * 通过匠人id获取工作室，匠人登陆后可查看自己的工作室
//     * @param craftsmanId
     * @return
     */
    @GetMapping("/get-workshop")
    public Result getWorkShopByCraftsmanId(){
//        log.info("id={}", craftsmanId);
//        Long craftsmanId = AuthContext.getUserId();
        Long craftsmanId = 2L;
        return workShopService.getWorkShopByCraftsmanId(craftsmanId);
    }

    /**
     * 获取工作室主页数据
     * @param id
     * @return
     */
    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable("id") Long id){
        log.info("id={}", id);
        return workShopService.getWorkShopDetail(id);
    }

    /**
     * 用户搜索工作室
     * @param workshopName
     * @return
     */
    @GetMapping("/nameselect")
    public Result selectWorkShopName(@RequestParam("workshopName") String workshopName){
        log.info("workshopName={}", workshopName);
        return workShopService.selectWorkShopName(workshopName);
    }

    /**
     * 匠人申请工作室
     * @param workShopDTO
     * @return
     */
    @PostMapping("/sign-up")
    public Result signUpWorkShop(@RequestBody @Validated WorkShopDTO workShopDTO){
        log.info("workShop={}", workShopDTO);
//        Long craftsmanId = AuthContext.getUserId();
        //TODO 测试环境下使用固定ID,记得修改
        Long craftsmanId = 2L;
        return workShopService.signUpWorkShop(craftsmanId,workShopDTO);
    }

    @PutMapping("/update")
    public Result  updateWorkShop(@RequestBody @Validated WorkShopDTO workShopDTO){
        log.info("workShop={}", workShopDTO);
        return workShopService.updateWorkShop(workShopDTO);
    }

    @PostMapping("/visit/{id}")
    public Result visitWorkShop(@PathVariable("id") Long id){
        log.info("id={}", id);
        return workShopService.visitWorkShop(id);
    }

    @PostMapping("/collect/{workShopId}")
    public Result collectWorkShop(@PathVariable("workShopId") Long workShopId){
        log.info("id={}", workShopId);
        return workShopService.collectWorkShop(workShopId);
    }


    @PutMapping("/set-workshopStatus")
    public Result working(@RequestParam Integer status){
//        Long craftsmanId = AuthContext.getUserId();
        //TODO 测试环境下使用固定ID,记得修改
        Long craftsmanId = 2L;
        return workShopService.setWorkShopStatus(craftsmanId,status);
    }

}
