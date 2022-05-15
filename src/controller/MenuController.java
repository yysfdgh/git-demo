package com.restaurant.graduate.controller;



import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.restaurant.graduate.bean.Bill;
import com.restaurant.graduate.bean.DiningTable;
import com.restaurant.graduate.bean.Menu;
import com.restaurant.graduate.service.BillingService;
import com.restaurant.graduate.service.DiningTableService;
import com.restaurant.graduate.service.MenuService;
import com.restaurant.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("All")
@RestController
@RequestMapping("/user")
@CrossOrigin
public class MenuController {
    @Autowired
    MenuService menuService;

    //查询菜单表所有数据
    @GetMapping("findAll")
    public R findAllMenu() {
        //调用service的方法实现查询所有的操作
        List<Menu> list = menuService.list(null);
        List<Object> objects = new ArrayList<>();
        for (Menu menu :list){
            objects.add(menu.getName());
        }
        return R.ok().data("items",objects);
    }

    //账单中根据name进行查询，总账单
    @PostMapping("getBill")
    public R getBill(@RequestBody Bill bill) {
        QueryWrapper<Bill> wrapper = new QueryWrapper<>();
        wrapper.eq("name",bill.getName());
        List<Bill> list = billingService.list(wrapper);
        int total=0;
        for (Bill bill1: list) {
            Integer nums = bill1.getNums();
            Integer price = bill1.getPrice();
            //总金额
            total += nums*price;
        }
        return R.ok().data("list",list).data("total",total);
    }

    //查询菜单 根据条件查询（分页）
    @PostMapping("/menu/{current}/{limit}")
    public R getMenuCondition(@PathVariable  int current,
                              @PathVariable  int limit,
                              @RequestBody(required = false) Menu menu){
        Page<Menu> pageParam = new Page<>(current,limit);
        QueryWrapper<Menu> wrapper = new QueryWrapper<>();
        String name = menu.getName();
        String type = menu.getType();
        Integer price = menu.getPrice();
        if(!(type==null||type.equals(""))){
            wrapper.eq("type",type);
        }
        if(!(price==null||price.equals(""))){
            wrapper.eq("price",price);
        }
        if(!(name==null||name.equals(""))){
            wrapper.like("name",name);
        }
        //排序
        wrapper.orderByAsc("price");
        menuService.page(pageParam,wrapper);
        List<Menu> records = pageParam.getRecords();//得到记录数
        long total = pageParam.getTotal();//得到总共的
        long current1 = pageParam.getCurrent();
        List<OrderItem> orders = pageParam.getOrders();
        return R.ok().data("total",total).data("rows",records);
    }



    //菜单条件查询条件
    @GetMapping("findCondition")
    public R findConditionMenu(Menu menu) {
        QueryWrapper<Menu> wrapper = new QueryWrapper<>();
        //调用service的方法实现查询所有的操作
        String name = menu.getName();
        String type = menu.getType();
        Integer price = menu.getPrice();
        if(!(type==null||type.equals(""))){
            wrapper.eq("type",type);
        }
        if(!(price==null||price.equals(""))){
            wrapper.eq("price",price);
        }
        if(!(name==null||name.equals(""))){
            wrapper.like("name",name);
        }
        List<Menu> list = menuService.list(wrapper);
        return R.ok().data("list",list);
    }

    //查询餐桌信息 返回餐桌状态，不含顾客名称和手机号
    @GetMapping("selectDining")
    public R selectDiningTable(){
        List<DiningTable> list = diningTableService.list();
        Map<String, String> str = new HashMap<>();
        return R.ok().data("list",list);
    }

    @Autowired
    DiningTableService diningTableService;
    //预定餐桌接口的方法
    @PostMapping("reserve")
    public R updateDiningTable(@RequestBody DiningTable diningTable) {
        try {
//        加条件判断这个座位是否被预定
            QueryWrapper<DiningTable> wrapper = new QueryWrapper<>();
            wrapper.eq("table_nbr", diningTable.getTableNbr());
            DiningTable one = diningTableService.getOne(wrapper);
            //如果名称相同，直接返回，，主要是应对点餐加bill表的逻辑
            if(one.getOrderName().equals(diningTable.getOrderName())){
                return R.ok();
            }
            //  可加条件判断是否为名字电话是否为空
            if (!(one.getState().equals("空"))) {
                return R.error().message("此座位有人占用，请选择其他座位");
            }
//        是空座位就执行接下来的方法
            DiningTable table = new DiningTable();
            table.setId(diningTable.getTableNbr());
            table.setOrderName(diningTable.getOrderName());
            table.setState(diningTable.getState());
            boolean save = diningTableService.updateById(table);
            if(save){
                return R.ok();
            }else{
                return R.error().message("添加失败");
            }
        }catch(NullPointerException e){
            return R.error().message("请填写预定信息");
        }
    }


    //保存订单,
    //前端没有菜品单价,这个方法中有封装价格
    @Autowired
    BillingService billingService;
    @PostMapping("addBill")
    public R addBill(@RequestBody Bill bill) {
        try {
            String dish = bill.getDish();
            QueryWrapper<Menu> wrapper = new QueryWrapper<>();
            wrapper.eq("name", dish);
            Menu one = menuService.getOne(wrapper);
            bill.setPrice(one.getPrice());
            boolean save = billingService.save(bill);
            if(save){
                return R.ok();
            }else{
                return R.error().message("添加失败");
            }
        }
        catch(Exception e){
            return R.error().message("请输入数据");

        }

    }


    //结账方法,逻辑删除对应姓名的数据
    @DeleteMapping("clearBill") //@PathVariable
    public R removeBill(@RequestBody Bill bill) {
        QueryWrapper<Bill> wrapper = new QueryWrapper<>();
        wrapper.eq("name",bill.getName());
        boolean flag = billingService.remove(wrapper);
        if(flag) {
            return R.ok();
        } else {
            return R.error();
        }
    }
   //更新餐桌状态
    @PostMapping("rese")
    public R updateDining(@RequestBody Bill bill) {
        //取得要修改餐桌的用户名
        DiningTable diningTable = new DiningTable();
        diningTable.setOrderName(bill.getName());
        //根据用户名进行查询
        QueryWrapper<DiningTable> wrapper = new QueryWrapper<>();
        wrapper.eq("order_name", diningTable.getOrderName());
        DiningTable one = diningTableService.getOne(wrapper);
        //判断是否 点过餐，或预定过
       if(ObjectUtils.isEmpty(one)) {
           return R.error().message("请先订餐品");
       }

       //更新用餐表
        one.setState("空");
        one.setOrderName("");
        one.setOrderTel("");
        one.setDescr("");
        boolean b = diningTableService.updateById(one);
        if(b){
            return R.ok();
        }else{
            return R.error().message("更新失败");
        }
    }
}
