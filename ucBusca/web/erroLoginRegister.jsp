<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css">
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Error Login/Register</title>
</head>
<body bgcolor= #f0ffff>
<nav class="navbar navbar-inverse">
    <div class="container-fluid">
        <div class="navbar-header">
            <a class="navbar-brand" href="#">UcBusca</a>
        </div>
        <ul class="nav navbar-nav">
            <li class="active"><a href="index.jsp">Pagina Inicial</a></li>
            <c:choose>
                <c:when test="${session.loggedin == true}">
                    <li><a href="<s:url action="history" />">MyHistory</a></li>
                </c:when>
            </c:choose>
            <c:choose>
                <c:when test="${session.admin == true}">
                    <li><a href="indexurl.jsp">Index Url</a> </li>
                    <li><a href="<s:url action="printadmins"/> ">Give Admin</a> </li>
                </c:when>
            </c:choose>
        </ul>
        <ul class="nav navbar-nav navbar-right">
            <c:choose>
                <c:when test="${session.loggedin == true}">
                    <li><a href="register.jsp">Log Out</a> </li>
                </c:when>
                <c:otherwise>
                    <li><a href="register.jsp">Sign Up</a> </li>
                    <li><a href= "login.jsp">Log In</a> </li>
                </c:otherwise>
            </c:choose>
        </ul>
    </div>
</nav>

<div class="container">
    <li><a><title>User Errado</title></a></li>
    <br>
    <li><a>Por favor, Dê Login na sua conta ou Registe-se</a></li>
    <br>
    <li><a href="register.jsp">Sign Up</a>
    <li><a href="login.jsp">Log in</a></li>
</div>
</body>
</html>