package dimaliahov.controller;

import dimaliahov.model.Product;
import dimaliahov.model.ProductIdAndCount;
import dimaliahov.model.User;
import dimaliahov.service.DAOFactory;
import dimaliahov.service.DAOInterfase.CartDAO;
import dimaliahov.service.DAOInterfase.ProductDAO;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServletCart extends HttpServlet {
	@Override
	protected void doGet (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		getMethods(req, resp);
	}

	@Override
	protected void doPost (HttpServletRequest req, HttpServletResponse resp) throws IOException {
		postMethods(req, resp);
	}

	private void getMethods (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		RequestDispatcher rd = req.getRequestDispatcher("WEB-INF/views/shoppingCart.jsp");
		HttpSession session = req.getSession();
		User user;
		user = (User) session.getAttribute("userPojo");

		int userId;
		userId = user.getId();

		DAOFactory daoFactory = DAOFactory.getInstance(1);
		CartDAO cartDAO = daoFactory.getCartDAO();
		ProductDAO productDAO = daoFactory.getProductDAO();

		List<ProductIdAndCount> productCount = cartDAO.getProductsAndCount(userId);
		List<Product> products = new ArrayList<>();
		for (ProductIdAndCount productIdAndCount : productCount) {
			Product p;
			p = productDAO.getProductByID(productIdAndCount.getProductId());
			p.setCount(productIdAndCount.getCount());
			products.add(p);
		}

		session.setAttribute("countCart", cartDAO.getCountProductByUserId(userId));
		session.setAttribute("cartProduct", products);

		rd.forward(req, resp);
	}

	private void postMethods (HttpServletRequest req, HttpServletResponse resp)throws IOException {
		int userId;
		int productId = -1;
		int count = -1;
		String removeStr;
		removeStr = req.getParameter("removeId");
		HttpSession session = req.getSession();
		User user;
		user = (User) session.getAttribute("userPojo");

		if (removeStr == null) {
			productId = Integer.parseInt(req.getParameter("idProduct"));
			count = Integer.parseInt(req.getParameter("amount"));
		}
		userId = user.getId();

		DAOFactory daoFactoryCart = DAOFactory.getInstance(1);
		CartDAO cartDAO = daoFactoryCart.getCartDAO();

		if (removeStr != null) {
			int removeId = Integer.parseInt(removeStr);

			cartDAO.removeProductFromCartById(userId, removeId);
			session.setAttribute("countCart", cartDAO.getCountProductByUserId(userId));
			resp.sendRedirect("/cart");

		} else if (productId != -1 && userId != -1) {

			List<Product> products;
			products = (List<Product>) session.getAttribute("cartProduct");

			Product p = cartDAO.getProductByProductIdAndUserId(userId, productId);
			if (p.getId() == 0) {
				cartDAO.addProductToCart(userId, productId, count);
			}
			for (Product product : products) {
				p = product;
				int id = p.getId();
				if (productId == id) {
					cartDAO.changeCountOnProduct(userId, productId, count);
					if (count <= 0) {
						cartDAO.removeProductFromCartById(userId, productId);
					}
					break;
				}

			}
			session.setAttribute("countCart", cartDAO.getCountProductByUserId(userId));
			resp.sendRedirect("/cart");
		}
	}
}
