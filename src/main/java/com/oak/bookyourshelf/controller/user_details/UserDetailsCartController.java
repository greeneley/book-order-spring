package com.oak.bookyourshelf.controller.user_details;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserDetailsCartController {

    @GetMapping("/user-details/cart")
    public String tab() {

        return "user_details/_cart";
    }
}
