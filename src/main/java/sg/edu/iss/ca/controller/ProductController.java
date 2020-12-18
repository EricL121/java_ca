package sg.edu.iss.ca.controller;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import sg.edu.iss.ca.model.Brand;
import sg.edu.iss.ca.model.Inventory;
import sg.edu.iss.ca.model.Product;
import sg.edu.iss.ca.service.ProductService;
import sg.edu.iss.ca.service.BrandService;
import sg.edu.iss.ca.service.ProductImplement;

@Controller
@RequestMapping("/product")
public class ProductController {
	@Autowired
	private ProductService pservice;
	
	@Autowired
	private BrandService brandSvc;
	
	@Autowired
	public void setProductservice(ProductImplement productimple) {
		this.pservice = productimple;
	}
    
	@RequestMapping(value = "/list")
	public String list(Model model) {
		return findPaginated(1,model);
	}
	@RequestMapping(value = "/add")
	public String addForm(Model model) {
		model.addAttribute("product", new Product());
		
		model.addAttribute("brandList", (ArrayList<Brand>)brandSvc.listAllBrands());
		return "ProductForm";
	}
	@RequestMapping(value = "/edit/{id}")
	public String editForm(@PathVariable("id") Integer id, Model model) {
		Product p = pservice.findProductById(id);
		
		if(p.getBrand() != null)
			p.setBrandName(p.getBrand().getName());
		
		model.addAttribute("product", p);
		model.addAttribute("brandList", (ArrayList<Brand>)brandSvc.listAllBrands());
		return "ProductForm";
	}
	@RequestMapping(value = "/save")
	public String addProduct(@ModelAttribute("product") @Valid Product product, 
			BindingResult bindingResult,  Model model) {
		if (bindingResult.hasErrors()) {
			return "ProductForm";
		}
				
		// Find if the name of the brand is in the database
		Brand b = brandSvc.findByBrandName(product.getBrandName());
		if(b != null)
			product.setBrand(b);
		else if(!product.getBrandName().trim().isEmpty()) {
			Brand newBrand = new Brand(product.getBrandName().trim());
			brandSvc.createBrand(newBrand);
			product.setBrand(newBrand);
		}
		
		pservice.createProduct(product);
		
		return "redirect:/product/list";
	}
	@RequestMapping(value = "/delete/{id}")
	public String deleteProduct(@PathVariable("id") Integer id) {
		pservice.deleteProduct(pservice.findProductById(id));
		return "redirect:/product/list";
	}
	@GetMapping("/page/{pageNo}")
	public String findPaginated(@PathVariable (value= "pageNo") int pageNo,Model model)
	{
		int pageSize=5;
		Page<Product> page=pservice.findPaginated(pageNo, pageSize);
		List<Product> listProducts=page.getContent();
		model.addAttribute("currentPage",pageNo);
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("totalItems",page.getTotalElements());
		model.addAttribute("ProductList", listProducts);
		return "ProductList";
	}

}
