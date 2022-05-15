package com.restaurant.graduate.controller;

import com.restaurant.utils.R;
import org.springframework.web.bind.annotation.*;
@RequestMapping("/user")
@RestController
@CrossOrigin //解决跨域
public class LoginController {
    //login
    @PostMapping(value={"/","/login"})
    public R login() {//框架是根据返  回值得到的token,即这里的admin

        return R.ok().data("token","admin");
    }
    //info
    @GetMapping("info")
    public R info() {
        //显示哪个用户登录
        return R.ok().data("roles","[admin]").
                data("name","admin").
                data("avatar","/src/assets/user.png");
    }

//    @Autowired
//    UserService userService;
//    //用户注册
//    @PostMapping("savemain")
//    public R savemain(@RequestBody User user){
//
//        userService.save(user);
//        return R.ok();
//    }

}
