<%--
  Created by IntelliJ IDEA.
  User: ADMIN
  Date: 09/11/2024
  Time: 12:17 CH
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <!-- Start Header/Navigation -->
  <nav class="custom-navbar navbar navbar navbar-expand-md navbar-dark bg-dark" arial-label="Furni navigation bar">

    <div class="container">
      <img class="navbar-brand" src="../images/logoFurni.png" alt="Furni Logo" style="height: 150px;">
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarsFurni" aria-controls="navbarsFurni" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="navbarsFurni">
        <ul class="custom-navbar-nav navbar-nav ms-auto mb-2 mb-md-0">
          <li class="nav-item ">
            <a class="nav-link" href="/KhachHang/index.jsp">Home</a>
          </li>
          <li><a class="nav-link" href="../shopServlet">Shop</a></li>
          <li><a class="nav-link" href="/KhachHang/about.jsp">About us</a></li>
          <li><a class="nav-link" href="/KhachHang/services.jsp">Services</a></li>
          <li><a class="nav-link" href="/KhachHang/blog.jsp">Blog</a></li>
          <li><a class="nav-link" href="/KhachHang/contact.jsp">Contact us</a></li>
          <% if (session.getAttribute("customer") != null) { %>
          <li>
            <a class="nav-link" href="<%= request.getContextPath() %>/Staff/loadStaffChatList">Chat (KH)</a>
          </li>
          <% } %>

          <%
            if(session.getAttribute("customer") == null && session.getAttribute("staff") == null && session.getAttribute("owner") == null){
          %>
            <li><a class="nav-link" href="/KhachHang/login.jsp">Login</a></li>
          <%
            } else {
          %>
            <li>
              <a class="nav-link" href="#" onclick="confirmLogout()">Logout</a>
            </li>
          <%
            }
          %>

        </ul>

        <ul class="custom-navbar-cta navbar-nav mb-2 mb-md-0 ms-5">
          <li><a class="nav-link" href="#"><img src="../images/user.svg"></a></li>
          <li><a class="nav-link" href="../PurchaseServlet"><img src="../images/cart.svg"></a></li>
          <li><a class="nav-link" href="manageOrdersServlet?action=loadOrders"><img src="../images/orders.svg" alt="Orders"></a></li>        </ul>
      </div>
    </div>

  </nav>
  <!-- End Header/Navigation -->
</html>
