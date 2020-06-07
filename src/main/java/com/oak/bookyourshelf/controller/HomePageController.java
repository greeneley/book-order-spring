package com.oak.bookyourshelf.controller;


import com.oak.bookyourshelf.Globals;
import com.oak.bookyourshelf.model.*;
import com.oak.bookyourshelf.service.AuthService;
import com.oak.bookyourshelf.service.CompareProductsService;
import com.oak.bookyourshelf.service.HomePageService;
import com.oak.bookyourshelf.service.ProductService;
import com.oak.bookyourshelf.service.product_details.ProductDetailsInformationService;
import com.oak.bookyourshelf.service.profile.ProfileInformationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class HomePageController {

    final AuthService authService;
    final ProfileInformationService profileInformationService;
    final HomePageService homePageService;
    final CompareProductsService compareProductsService;
    final ProductService productService;
    final ProductDetailsInformationService productDetailsInformationService;


    public HomePageController(AuthService authService, ProfileInformationService profileInformationService, HomePageService homePageService, CompareProductsService compareProductsService, ProductService productService, ProductDetailsInformationService productDetailsInformationService) {
        this.authService = authService;
        this.profileInformationService = profileInformationService;
        this.homePageService = homePageService;
        this.compareProductsService = compareProductsService;
        this.productService = productService;
        this.productDetailsInformationService = productDetailsInformationService;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String showUser(Model model) {

        UserDetails userDetails = authService.getUserDetails();
        List<Product> ourPicksForYou = new ArrayList<>();
        if (userDetails != null) {
            model.addAttribute("bestSeller", homePageService.createBestSellerList(0, 8));

            model.addAttribute("newProducts", homePageService.createNewProductsList(0, 8));

            model.addAttribute("moreBestSeller", homePageService.createBestSellerList(8, 15));

            model.addAttribute("moreNewProducts", homePageService.createNewProductsList(8, 15));

            model.addAttribute("ourPicks_1", homePageService.createBestSellerList(0, 4));

            model.addAttribute("ourPicks_2", homePageService.createBestSellerList(4, 8));

            model.addAttribute("ourPicks_3", homePageService.createBestSellerList(8, 12));

            model.addAttribute("user", profileInformationService.getByEmail(userDetails.getUsername()));

        } else {
            model.addAttribute("bestSeller", homePageService.createBestSellerList(0, 8));

            model.addAttribute("newProducts", homePageService.createNewProductsList(0, 8));

            model.addAttribute("moreBestSeller", homePageService.createBestSellerList(8, 15));

            model.addAttribute("moreNewProducts", homePageService.createNewProductsList(8, 15));

            model.addAttribute("ourPicks_1", ourPicksForYou);
        }
        model.addAttribute("compareProductsService", compareProductsService);
        return "/index";
    }


    @RequestMapping(value = "/index/{id}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> addProductToList(@RequestParam String type, @PathVariable int id) {

        UserDetails userDetails = authService.getUserDetails();
        Product product = productService.get(id);

        if (userDetails != null) {
            User user = profileInformationService.getByEmail(userDetails.getUsername());

            switch (type) {
                case "wish-list":
                    if (!containsProduct(user.getWishList(), product.getProductId())) {
                        user.addToWishList(product);
                        profileInformationService.save(user);
                    }
                    // already in wish list
                    return ResponseEntity.ok("");


                case "cart":
                    if (!containsCartItem(user.getShoppingCart(), product.getProductId())) {
                        if (product.getStock() > 0) {
                            CartItem cartItem = new CartItem();
                            cartItem.setProduct(product);
                            cartItem.setQuantity(1);
                            user.addToCart(cartItem);
                            profileInformationService.save(user);
                            return ResponseEntity.ok("");

                        } else {
                            return ResponseEntity.badRequest().body("There is no stock.");
                        }
                    } else {
                        // already in cart, quantity updated, save user
                        profileInformationService.save(user);
                        return ResponseEntity.ok("");
                    }




            }
        } else {
            // direct to login page
            return ResponseEntity.badRequest().body("");
        }

        return ResponseEntity.badRequest().body("An error occurred.");
    }


    public boolean containsCartItem(Set<CartItem> set, int productId) {
        for (CartItem c: set) {
            if(c.getProduct().getProductId() == productId) {
                c.setQuantity(c.getQuantity() + 1);
                return true;
            }
        }
        return false;
    }

    public boolean containsProduct(Set<Product> set, int productId) {
        for (Product p: set) {
            if(p.getProductId() == productId) {
                return true;
            }
        }
        return false;
    }


}
