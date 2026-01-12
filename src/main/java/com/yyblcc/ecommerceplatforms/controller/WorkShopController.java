package com.yyblcc.ecommerceplatforms.controller;

import com.yyblcc.ecommerceplatforms.domain.DTO.WorkShopDTO;
import com.yyblcc.ecommerceplatforms.domain.VO.WorkShopVO;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.PageQuery;
import com.yyblcc.ecommerceplatforms.service.WorkShopService;
import com.yyblcc.ecommerceplatforms.util.StpKit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workshop")
@Slf4j
@RequiredArgsConstructor
public class WorkShopController {

    private final WorkShopService workShopService;

    @PostMapping
    public Result signUp(@RequestBody @Validated WorkShopDTO workShopDTO){
        log.info("workShop={}", workShopDTO);
        Long craftsmanId = StpKit.CRAFTSMAN.getLoginIdAsLong();
        return workShopService.signUpWorkShop(craftsmanId,workShopDTO);
    }

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
     * @param query
     * @return
     */
    @GetMapping("/page")
    public Result page(PageQuery query){
        log.info("分页查询query:{}",query);
        return workShopService.pageWorkShop(query);
    }

    /**
     * 通过匠人id获取工作室，匠人登陆后可查看自己的工作室
     * @return
     */
    @GetMapping("/get-workshop")
    public Result getWorkShopByCraftsmanId(){
        Long craftsmanId = StpKit.CRAFTSMAN.getLoginIdAsLong();
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
        Long craftsmanId = StpKit.CRAFTSMAN.getLoginIdAsLong();
        return workShopService.signUpWorkShop(craftsmanId,workShopDTO);
    }

    @PutMapping("/update")
    public Result  updateWorkShop(@RequestBody WorkShopDTO workShopDTO){
        log.info("workShop={}", workShopDTO);
        return workShopService.updateWorkShop(workShopDTO);
    }

    @PostMapping("/visit")
    public Result visitWorkShop(@RequestParam Long id){
        log.info("id={}", id);
        return workShopService.visitWorkShop(id);
    }

    @PostMapping("/collect/{workShopId}")
    public Result collectWorkShop(@PathVariable("workShopId") Long workShopId){
        log.info("id={}", workShopId);
        return workShopService.collectWorkShop(workShopId);
    }


    @GetMapping("/getShopStatus")
    public Result getShopStatus(){
        Long craftsmanId = StpKit.CRAFTSMAN.getLoginIdAsLong();
        return workShopService.getWorkShopStatus(craftsmanId);
    }

    @PutMapping("/set-workshopStatus")
    public Result working(@RequestParam Integer status){
        Long craftsmanId = StpKit.CRAFTSMAN.getLoginIdAsLong();
        return workShopService.setWorkShopStatus(craftsmanId,status);
    }

    @PostMapping("/view")
    public void viewWorkShop(@RequestParam Long id){
        log.info("id={}", id);
        workShopService.viewWorkShop(id);
    }

    @GetMapping("/front-page")
    public Result<PageBean<WorkShopVO>> frontPage(PageQuery query){
        log.info("用户分页查询工作室");
        return workShopService.frontPage(query);
    }

    @GetMapping("/checkCollect")
    public Result<Boolean> checkCollect(@RequestParam Long workShopId){
        log.info("id={}", workShopId);
        return workShopService.checkCollect(workShopId);
    }

    @PutMapping("/masterPieces")
    public Result<?> updateMasterPieces(@RequestBody List<String> masterPieces) {
        log.info("更新匠人作品: masterPieces={}", masterPieces);
        return workShopService.updateMasterPieces(masterPieces);
    }

}
