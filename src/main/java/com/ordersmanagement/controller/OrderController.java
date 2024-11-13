package com.ordersmanagement.controller;

import com.ordersmanagement.model.Order;
import com.ordersmanagement.model.Product;
import com.ordersmanagement.model.ProductType;
import com.ordersmanagement.service.OrderService;
import com.ordersmanagement.service.ProductService;
import com.ordersmanagement.service.ProductTypeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;


@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductTypeService productTypeService;

    @Autowired
    private ProductService productService;



    @GetMapping("/list")
    public String listOrders(
            Model model,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "topOrders", required = false) Integer topOrders,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate endDate) {

        Page<?> orderPage;

        if (topOrders != null) {
            // Vẫn giữ phân trang khi xem top orders
            orderPage = orderService.getTopOrdersByTotalAmount(topOrders, page, size);
            model.addAttribute("topOrders", topOrders);
        } else if (startDate != null && endDate != null) {
            if (endDate.isBefore(startDate)) {
                model.addAttribute("errorMessage", "Ngày kết thúc phải sau ngày bắt đầu!");
                orderPage = orderService.getAllOrdersPaged(page, size);
            } else {
                orderPage = orderService.getOrdersByDateRange(startDate, endDate, page, size);
                model.addAttribute("startDate", startDate);
                model.addAttribute("endDate", endDate);
            }
        } else {
            orderPage = orderService.getAllOrdersPaged(page, size);
        }

        addPagingAttributes(model, orderPage, page);
        return "orders/list";
    }

    private void addPagingAttributes(Model model, Page<?> page, int currentPage) {
        model.addAttribute("orders", page.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderById(id);
        List<ProductType> productTypes = productTypeService.getAllProductTypes();
        List<Product> products = productService.getAllProducts();

        model.addAttribute("order", order);
        model.addAttribute("productTypes", productTypes);
        model.addAttribute("products", products);

        return "orders/edit";
    }

    @PostMapping("/update")
    public String updateOrder(@ModelAttribute @Valid Order order,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            List<ProductType> productTypes = productTypeService.getAllProductTypes();
            List<Product> products = productService.getAllProducts();
            model.addAttribute("productTypes", productTypes);
            model.addAttribute("products", products);
            return "orders/edit";
        }

        orderService.updateOrder(order);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật đơn hàng thành công!");
        return "redirect:/orders/list";
    }

    @GetMapping("/products-by-type")
    @ResponseBody
    public List<Product> getProductsByType(@RequestParam Long typeId) {
        return productService.getProductsByProductTypeId(typeId);
    }
}