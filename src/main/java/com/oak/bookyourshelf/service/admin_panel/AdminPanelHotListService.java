package com.oak.bookyourshelf.service.admin_panel;

import com.oak.bookyourshelf.model.Category;
import com.oak.bookyourshelf.model.HotList;
import com.oak.bookyourshelf.model.Product;
import com.oak.bookyourshelf.repository.admin_panel.AdminPanelHotListRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class AdminPanelHotListService {
    final AdminPanelHotListRepository adminPanelHotListRepository;

    public AdminPanelHotListService(AdminPanelHotListRepository adminPanelHotListRepository) {
        this.adminPanelHotListRepository = adminPanelHotListRepository;
    }

    public Iterable<HotList> listAll() {
        return adminPanelHotListRepository.findAll();
    }

    public void save(HotList hotList) {
        adminPanelHotListRepository.save(hotList);
    }

    public HotList get(int id) {
        return adminPanelHotListRepository.findById(id).get();
    }

    public HotList getByHotListType(String name) {
        return adminPanelHotListRepository.findHotListByHotListType(name);
    }

    public List<HotList> getAllByName(String name) {
        return adminPanelHotListRepository.findAllByHotListType(name);
    }

    public void delete(int id) {
        adminPanelHotListRepository.deleteById(id);
    }

    public List<HotList> findAllByProductType(Category.ProductType type) {
        return adminPanelHotListRepository.findAllByProductType(type);
    }

    /*
    public Set<Product> createProductSet(List<Subcategory> subcategories)
    {
        Set<Product> allProducts = Collections.emptySet();
        for(Subcategory sub :subcategories)
        {
            for(Product p:sub.getProducts())
            {
                allProducts.add(p);
            }
        }
        return allProducts;
    }

 */

    public void setProductsRate(Set<Product> allProducts, int rate)
    {
        for(Product p :allProducts)
        {
            p.setOnDiscount(true);
            p.setDiscountRate((float)rate);
        }

    }


}
