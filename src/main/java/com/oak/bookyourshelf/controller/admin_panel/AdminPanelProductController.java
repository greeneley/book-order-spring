package com.oak.bookyourshelf.controller.admin_panel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oak.bookyourshelf.Globals;
import com.oak.bookyourshelf.model.*;
import com.oak.bookyourshelf.service.admin_panel.AdminPanelCategoryService;
import com.oak.bookyourshelf.service.admin_panel.AdminPanelProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class AdminPanelProductController {

    final AdminPanelProductService adminPanelProductService;
    final AdminPanelCategoryService adminPanelCategoryService;

    public AdminPanelProductController(AdminPanelProductService adminPanelProductService, AdminPanelCategoryService adminPanelCategoryService) {
        this.adminPanelProductService = adminPanelProductService;
        this.adminPanelCategoryService = adminPanelCategoryService;
    }

    @RequestMapping(value = "/admin-panel/product", method = RequestMethod.GET)
    public String tab(@RequestParam("page") Optional<Integer> page,
                      @RequestParam("size") Optional<Integer> size,
                      @RequestParam("sort") Optional<String> sort, Model model) {

        String currentSort = sort.orElse("date");
        Globals.getPageNumbers(page, size, adminPanelProductService.sortProducts(currentSort), model, "productPage");
        model.addAttribute("allProducts", adminPanelProductService.listAll());
        model.addAttribute("categoryService", adminPanelCategoryService);
        model.addAttribute("sort", currentSort);
        return "admin_panel/_product";
    }

    @RequestMapping(value = "/admin-panel/product/subcategory", method = RequestMethod.GET)
    @ResponseBody
    public List<Subcategory> findAllSubcategories(@RequestParam String category) {
        return Globals.getAllSubcategories(adminPanelCategoryService.getByName(category));
    }

    @RequestMapping(value = "/admin-panel/product", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> saveProduct(@RequestParam String productType, PhysicalBook physicalBook,
                                              ElectronicBook electronicBook, AudioBook audioBook,
                                              ElectronicBookReader electronicBookReader,
                                              ElectronicBookReaderCase electronicBookReaderCase,
                                              PhysicalBookCase physicalBookCase,
                                              @RequestParam("lists") String lists,
                                              @RequestParam("category_name") String category_name,
                                              @RequestParam("subcategory_name") String subcategory_name) throws JsonProcessingException {
        Category category;
        Subcategory subcategory;


        switch (productType) {
            case "book":
                category = adminPanelCategoryService.getByName(category_name);
                subcategory = adminPanelCategoryService.getSubcategory(category, subcategory_name);
                physicalBook.setCategory(new ArrayList<Category>());
                physicalBook.getCategory().add(category);
                physicalBook.setSubcategory(new ArrayList<Subcategory>());
                physicalBook.getSubcategory().add(subcategory);
                return bookBarcodeAndISBNCheck(physicalBook, lists);
            case "ebook":
                category = adminPanelCategoryService.getByName(category_name);
                subcategory = adminPanelCategoryService.getSubcategory(category, subcategory_name);
                physicalBook.setCategory(new ArrayList<Category>());
                physicalBook.getCategory().add(category);
                physicalBook.setSubcategory(new ArrayList<Subcategory>());
                physicalBook.getSubcategory().add(subcategory);
                return bookBarcodeAndISBNCheck(electronicBook, lists);
            case "audio_book":
                category = adminPanelCategoryService.getByName(category_name);
                subcategory = adminPanelCategoryService.getSubcategory(category, subcategory_name);
                physicalBook.setCategory(new ArrayList<Category>());
                physicalBook.getCategory().add(category);
                physicalBook.setSubcategory(new ArrayList<Subcategory>());
                physicalBook.getSubcategory().add(subcategory);
                return bookBarcodeAndISBNCheck(audioBook, lists);
            case "ebook_reader":
                return productBarcodeCheck(electronicBookReader);
            case "ebook_reader_case":
                return productBarcodeCheck(electronicBookReaderCase);
            case "book_case":
                return productBarcodeCheck(physicalBookCase);
            default:
                return ResponseEntity.badRequest().body("An error occurred.");
        }
    }

    public ResponseEntity<String> productBarcodeCheck(Product product) {

        Product productDb = adminPanelProductService.getByBarcode(product.getBarcode());
        if (productDb != null) {
            return ResponseEntity.badRequest().body("Product with given barcode already exists.");
        }

        java.util.Date utilDate = new java.util.Date();
        java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
        product.setUploadDate(sqlDate);
        adminPanelProductService.save(product);
        return ResponseEntity.ok("");
    }

    public ResponseEntity<String> bookBarcodeAndISBNCheck(Book product, String lists) throws JsonProcessingException {

        // Add lists to product
        ObjectMapper mapper = new ObjectMapper();
        Map<String, ArrayList<String>> map = mapper.readValue(lists, Map.class);
        product.setPublishers(trimList(map.get("publishers")));
        product.setTranslators(trimList(map.get("translators")));
        product.setAuthors(trimList(map.get("authors")));
        product.setKeywords(trimList(map.get("keywords")));

        Product barcodeDb = adminPanelProductService.getByBarcode(product.getBarcode());
        if (barcodeDb != null) {
            return ResponseEntity.badRequest().body("Product with given barcode already exists.");
        }

        Product ISBNDb = adminPanelProductService.getByISBN(product.getIsbn());
        if (ISBNDb != null) {
            return ResponseEntity.badRequest().body("Product with given ISBN already exists.");
        }

        java.util.Date utilDate = new java.util.Date();
        java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
        product.setUploadDate(sqlDate);
        adminPanelProductService.save(product);
        return ResponseEntity.ok("");
    }

    public static List<String> trimList(List<String> list) {
        List<String> trimmedList = list.stream().map(String::trim).collect(Collectors.toList());
        trimmedList.removeIf(s -> s.equals(""));
        return trimmedList;
    }
}
