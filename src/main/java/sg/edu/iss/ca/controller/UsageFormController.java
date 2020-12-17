package sg.edu.iss.ca.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import sg.edu.iss.ca.model.ChangeQtyInput;
import sg.edu.iss.ca.model.FormCart;
import sg.edu.iss.ca.model.Product;
import sg.edu.iss.ca.model.UsageForm;
import sg.edu.iss.ca.repo.FormCartRepository;
import sg.edu.iss.ca.repo.ProductRepository;
import sg.edu.iss.ca.repo.UsageFormRepository;
import sg.edu.iss.ca.service.FormCartImplement;
import sg.edu.iss.ca.service.FormCartService;
import sg.edu.iss.ca.service.UsageFormImplement;
import sg.edu.iss.ca.service.UsageFormService;

@Controller
@RequestMapping("/UsageForm")
public class UsageFormController {
	@Autowired
	private ProductRepository prepo;
	
	@Autowired
	private UsageFormRepository ufrepo;
	
	@Autowired
	private FormCartRepository fcrepo;
	
	@Autowired
	private UsageFormService ufservice;
	
	@Autowired
	public void setUsageFormService(UsageFormImplement ufimp) {
		this.ufservice = ufimp;
	}
	
	@Autowired
	private FormCartService fcservice;
	
	@Autowired
	public void setFormCartService(FormCartImplement fcimp) {
		this.fcservice = fcimp;
	}
	
//	String sessionId = "123";
//	int userid = 456;
	
//	@RequestMapping(value = "/details/{id}")
//	public String showDetails(@PathVariable("id") int id, Model model) {
//		model.addAttribute("cartList", ufservice.listAllItems(ufrepo.findById(id).get()));
//		return "UsageForm";
//	}
	
	@RequestMapping(value = "/addProduct")
	public String addProduct(Model model, HttpSession session) {
		UsageForm uf = (UsageForm) session.getAttribute("UsageForm");
		model.addAttribute("UsageForm", uf);
		return "redirect:/product/list";
	}
	
	@RequestMapping(value = "/add")
	public String createForm(Model model, HttpSession session) {
		UsageForm usageForm = new UsageForm();
		ufrepo.save(usageForm);
		session.setAttribute("UsageForm", usageForm);
		//model.addAttribute("session", session);
		model.addAttribute("UsageForm", usageForm);
		model.addAttribute("cartList", new ArrayList<FormCart>());
		return "UsageForm";
	}
	
	@RequestMapping(value = "/details")
	public String viewForm(Model model, HttpSession session) {
		// hard coded formId
		// need some logic here to check if the current user has created a form
		// and get the Usage Form id
//		int id = 1;
		UsageForm uf = (UsageForm) session.getAttribute("UsageForm");
		int id = uf.getId();
		// int id = usageForm.getId();
		
//		if (ufrepo.existsById(id) == false)
//			ufservice.createForm();
		//model.addAttribute("session", session);
		model.addAttribute("UsageForm", uf);
		model.addAttribute("cartList", ufservice.listAllItems(ufrepo.findById(id).get()));
		return "UsageForm";
	}
	
	@RequestMapping(value = "/remove/{id}")
	public String removeItem(@PathVariable("id") int id) {
		fcservice.deleteCart(fcservice.findFormCartById(id));
		return "redirect:/UsageForm/details";
	}
	
	
	@PostMapping(value = "/ChangeCartQty", produces = "application/json")
	@ResponseBody
	public Map ChangeCartQty(@RequestBody ChangeQtyInput changeQtyInput, HttpSession session) {
		// hard coded formId
		// int id = 1;
		
		UsageForm uf = (UsageForm) session.getAttribute("UsageForm");
		int id = uf.getId();
		
		int productIdNum = changeQtyInput.getProductId();
		List<FormCart> fcl = ufservice.listAllItems(ufrepo.findById(id).get());
		FormCart fc = fcservice.findFormCartByProductIdAndFormId(productIdNum, id);
		
		if (changeQtyInput.getAction().equals("minus") && fc.getQty() > 1) {
			int qty = fc.getQty();
			fc.setQty(qty-1);
			fcrepo.save(fc);
		}
		
		// need to implement validation on stock count
		else if (changeQtyInput.getAction().equals("plus")) {
			int qty = fc.getQty();
			fc.setQty(qty+1);
			fcrepo.save(fc);
		}
		
		return Collections.singletonMap("status", "success");
	}
	
	
	@PostMapping(value = "/save")
	public String transactionSave(@ModelAttribute("UsageForm") @Valid UsageForm usageForm, 
			BindingResult bindingResult, Model model, HttpSession session) {
		if (bindingResult.hasErrors())
			return "redirect:/UsageForm/details";
		
		// hard coded formId
		// int id = 1;
		UsageForm uf = (UsageForm) session.getAttribute("UsageForm");
		int id = uf.getId();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = sdf.format(new Date());
		uf.setCustomer(usageForm.getCustomer());
		uf.setCar(usageForm.getCar());
		uf.setDescription(usageForm.getDescription());
		uf.setCreationDate(date);
		ufrepo.save(uf);
		List<FormCart> fcl = ufservice.listAllItems(ufrepo.findById(id).get());
		
//		model.addAttribute("date", date);
//		model.addAttribute("description", uf.getDescription());
//		model.addAttribute("customer", uf.getCustomer());
		model.addAttribute("UsageForm", uf);
		model.addAttribute("cartList", fcl);
		
		session.removeAttribute("UsageForm");
		
		return "TransactionSummary";
	}
	
	
	@RequestMapping(value = "/checkHistory/{id}")
	public String checkHistory(@PathVariable("id") int pid, Model model) {
		Product product = prepo.findProductById(pid);
		List<FormCart> fcl = fcservice.findFormCartsByProductId(pid);
		List<UsageForm> ufl = ufservice.findUsageFormsByProductId(pid);
		
		if (fcl == null) {
			return "NoTransHistory";
		}
		
		model.addAttribute("Product", product);
		model.addAttribute("UsageForm", ufl);
		model.addAttribute("cartList", fcl);
		return "PartTransHistory";
	}
	
//	@RequestMapping(value = "/ChangeCartQty")
//	public String ChangeCartQty(@RequestBody ChangeQtyInput changeQtyInput) {
//		int id = 1;
//		
//		int productIdNum = changeQtyInput.getProductId();
//		List<FormCart> fcl = ufservice.listAllItems(ufrepo.findById(id).get());
//		FormCart fc = fcservice.findFormCartByProductIdAndFormId(productIdNum, id);
//		
//		if (changeQtyInput.getAction() == "minus" && fc.getQty() > 1) {
//			int qty = fc.getQty();
//			fc.setQty(qty+1);
//			fcrepo.save(fc);
//		}
//		// need to implement validation on stock count
//		else if (changeQtyInput.getAction() == "plus") {
//			int qty = fc.getQty();
//			fc.setQty(qty-1);
//			fcrepo.save(fc);
//		}
//		
//		return "UsageForm";
//	}
	
}
