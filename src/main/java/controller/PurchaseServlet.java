package controller;

import ENumeration.EOrderStatus;
import VietQR.BankService;
import VietQR.QRCodeRequest;
import VietQR.QRCodeResponse;
import business.*;
import data.*;

import javax.servlet.*;

import javax.servlet.http.*;
import javax.servlet.annotation.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@WebServlet(name = "PurchaseServlet", value = "/PurchaseServlet")
public class PurchaseServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String url = "/cart.jsp";
        ServletContext sc = getServletContext();
        String action = request.getParameter("action");
        if(action == null) {
            action = "viewcart";
        }
        HttpSession session = request.getSession(); // không tạo sesion mới nếu có
        Customer customer = (Customer) session.getAttribute("customer");
        if(customer == null) {
            url = "/login.jsp";
            sc.getRequestDispatcher(url).forward(request, response);
            return;  // Dừng thực thi ở đây
        }

        if (action.equals("addtocart")) {
            url = "/shop.jsp";
            int furnitureID = Integer.parseInt(request.getParameter("furnitureID"));
            Furniture fur = FurnitureDB.getFurnitureByID(furnitureID);
            boolean check = CartDB.addToCart(customer, fur);
            if (check) {
                request.setAttribute("message", "Thêm thành công!");
            } else {
                request.setAttribute("message", "Sản phẩm đã có trong giỏ hàng!");
            }
            sc.getRequestDispatcher(url).forward(request, response);
        } else if (action.equals("viewcart")) {
            Cart cart = CartDB.getCart(customer);

            if(cart == null) {
                cart = new Cart();
                cart.setCustomer(customer);
                CartDB.insert(cart);
            }

            request.setAttribute("listFurniture", cart.getListFurniture());
            sc.getRequestDispatcher(url).forward(request, response);

        } else if(action.equals("removefromcart")) {

            int furnitureID = Integer.parseInt(request.getParameter("furnitureID"));
            Furniture fur = FurnitureDB.getFurnitureByID(furnitureID);
            Cart cart = CartDB.removeToCart(customer,fur);
            request.setAttribute("listFurniture", cart.getListFurniture());
            sc.getRequestDispatcher(url).forward(request, response);
        } else if (action.equals("purchase")) { // thiếu coupon !! sửa ở đây
            url = "/checkout.jsp";
            String listID = request.getParameter("listFurnitureID"); //Toàn bộ tên được chọn
            if (listID != null && !listID.isEmpty()) {
                // Chuyển chuỗi thành mảng các ID sản phẩm
                String[] listFurID = listID.split(",");
                List<Furniture> listFurniture= new ArrayList<>();
                int total = 0;
                for (int i = 0; i < listFurID.length; i++) {
                    Furniture fur = FurnitureDB.getFurnitureByID(Integer.parseInt(listFurID[i]));
                    listFurniture.add(fur);
                    total += fur.getFurniturePrice();
                }

                List<Coupon> listCoupon = CouponDB.getListCoupon(listFurniture);
                session.setAttribute("listCoupon", listCoupon);
                request.setAttribute("total", total);
                request.setAttribute("listFurniture", listFurniture);
                request.setAttribute("customer", customer);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmmss");
                String dateTimeNow = sdf.format(new Date());
                request.setAttribute("dateTimeNow", dateTimeNow);
                sc.getRequestDispatcher(url).forward(request, response);

            }
        } else if (action.equals("QRCODE")) {
            String[] listCategoryID = request.getParameterValues("listCategoryID"); //Toàn bộ tên được chọn
            String[] quantityStrings = request.getParameterValues("quantity");
            int[] quantities = new int[quantityStrings.length];
            for (int i = 0; i < quantityStrings.length; i++) {
                quantities[i] = Integer.parseInt(quantityStrings[i]);
            }
            boolean check = false;
            if (listCategoryID != null && quantities != null) {
                for (int i = 0; i < listCategoryID.length; i++) {
                    List<Furniture> furnitures = FurnitureDB.getFurnitureQuantity(listCategoryID[i], quantities[i]);
                    if (furnitures == null) {
                        check = true; // không đủ hàng
                        break;
                    }
                }
            }
            if (check == true) {
                String flag = "False";
                response.setContentType("text/plain");  // Đảm bảo kiểu phản hồi là văn bản
                response.getWriter().write(flag);  // Trả về kết quả sai báo không đủ hàng
            }
            else
            {
                String accountName = "DANG BA HIEN";
                long accountNumber = 797322626;
                int amount = Integer.parseInt(request.getParameter("amount"));
                String description = request.getParameter("description");
                int acqId = 970422;
                QRCodeRequest QRrequest = new QRCodeRequest(accountNumber, accountName, acqId, amount, description, "text", "print");
                BankService bankService = new BankService();
                QRCodeResponse QRresponse = bankService.createQRCode(QRrequest);
                QRresponse.data.setAcpId(acqId);
                QRresponse.data.setAccountName(accountName);
                response.setContentType("text/plain");  // Đảm bảo kiểu phản hồi là văn bản
                response.getWriter().write(QRresponse.getData().qrDataURL);  // Trả về URL mã QR
            }

        }else if (action.equals("coupon")){
            String couponCode = request.getParameter("couponCode");
            int total = Integer.parseInt(request.getParameter("total"));
            List<Coupon> listCoupon = (List<Coupon>) session.getAttribute("listCoupon");

            String[] listCategoryID = request.getParameterValues("listCategoryID"); //Toàn bộ tên được chọn
            String[] quantityStrings = request.getParameterValues("quantity");
            int[] quantities = new int[quantityStrings.length];
            for (int i = 0; i < quantityStrings.length; i++) {
                quantities[i] = Integer.parseInt(quantityStrings[i]);
            }
            List<Furniture> list = new ArrayList<>();

            if (listCategoryID != null && quantities != null) {
                for (int i = 0; i < listCategoryID.length; i++) {
                    list.add(FurnitureDB.getFurnitureDiscount(listCategoryID[i]));
                }
            }

            Coupon selectedCoupon = null;

            if (listCoupon != null) {
                for (Coupon coupon : listCoupon) {
                    if (coupon.getCouponID().equals(couponCode)) {
                        selectedCoupon = coupon;
                        break;
                    }
                }
            }
            double Giamgia = selectedCoupon.calculateDiscount(list,quantities,total);
            String GiamgiaString = String.valueOf(Giamgia);
            System.out.println(GiamgiaString);
            if(selectedCoupon.getUseLimit() <= selectedCoupon.getCurrentUsage())
            {
                String flag = "NoCoupon";
                response.setContentType("text/plain");
                response.getWriter().write(flag);            }
            else
            {
                session.setAttribute("selectedCoupon", selectedCoupon);
                response.setContentType("text/plain");
                response.getWriter().write(GiamgiaString);
            }
        }
        else if (action.equals("payment")) {
            String paymentMethod = request.getParameter("paymentMethod");
            System.out.println(paymentMethod);
            String[] listCategoryID = request.getParameterValues("listCategoryID"); //Toàn bộ tên được chọn
            String[] quantityStrings = request.getParameterValues("quantity");
            int[] quantities = new int[quantityStrings.length];
            for (int i = 0; i < quantityStrings.length; i++) {
                quantities[i] = Integer.parseInt(quantityStrings[i]);
            }
            List<Furniture> list = new ArrayList<>();
            boolean check = false;
            if (listCategoryID != null && quantities != null) {
                for (int i = 0; i < listCategoryID.length; i++) {
                    List<Furniture> furnitures = FurnitureDB.getFurnitureQuantity(listCategoryID[i], quantities[i]);
                    if (furnitures != null) {

                        list.addAll(furnitures);
                    }
                    else {
                        check = true; // không đủ hàng
                        break;
                    }
                }
            }
            if (check == true) {
                String flag = "False";
                response.getWriter().write(flag);  // Trả về kết quả sai báo không đủ hàng
            }
            else
            {
                Date orderDate = new Date();
                EOrderStatus orderStatus = EOrderStatus.WAITING_PROCESS;
                Date paymentDate = null;
                Order order = new Order(list,customer,orderDate,orderStatus);
                Payment payment;
                Coupon coupon = (Coupon) session.getAttribute("selectedCoupon");
                Double money = Double.parseDouble(request.getParameter("amount"));
                if(paymentMethod.equals("bank"))
                {
                    paymentDate = orderDate;
                }
                payment = new Payment(coupon,paymentDate,money,paymentMethod,order);

                if (OrderDB.insertOrder(order) && PaymentDB.insert(payment)) {
                    String flag = "True";
                    response.getWriter().write(flag);
                } else {
                    String flag = "False";
                    response.getWriter().write(flag);
                }
            }
        }
    }
}