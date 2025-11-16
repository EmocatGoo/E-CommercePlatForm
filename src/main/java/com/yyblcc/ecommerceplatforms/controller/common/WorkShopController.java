package com.yyblcc.ecommerceplatforms.controller.common;

import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.service.WorkShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/workshop")
@Slf4j
public class WorkShopController {
    @Autowired
    private WorkShopService workShopService;

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

    @GetMapping("/nameselect")
    public Result selectWorkShopName(@RequestParam("workshopName") String workshopName){
        log.info("workshopName={}", workshopName);
        return workShopService.selectWorkShopName(workshopName);
    }
}
