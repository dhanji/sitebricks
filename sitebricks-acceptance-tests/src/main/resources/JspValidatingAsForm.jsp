<%@ page language="java" contentType="text/jsp;charset=UTF-8"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.List" %>

<html>
<!--
Design by Free CSS Templates
http://www.freecsstemplates.org
Released for free under a Creative Commons Attribution 2.5 License
-->
<head>
    <link href="default.css" rel="stylesheet" type="text/css" />
</head>

<body>
    <!-- start header -->
    <div id="header">
        <div id="logo">
            <h1>Sitebricks</h1>
            <h2>a fast, light web framework from Google</h2>
        </div>
        <div id="menu">
            <ul>
                <li class="active"><a href="http://code.google.com/p/google-sitebricks">Google Sitebricks</a></li>

                <li><a href="http://google-sitebricks.googlecode.com/svn/trunk">source</a></li>
                <li><a href="http://code.google.com/p/google-sitebricks/wiki/list">doc</a></li>
                <li><a href="http://code.google.com/p/google-guice">guice</a></li>
            </ul>
        </div>
    </div>

    <div id="page">
        <!-- start content -->
        <div id="content">

        <div>Any validation errors here?:</div>
<%--
The following JAVA scriptlet can be eaislly replaced with a custom
taglib to make a nicer integration in this JSP.
--%>
<%
Object obj = request.getAttribute("pageFlowErrors");
if (obj != null) {
    out.println("<div class=\"errors\">");
    out.println("<ul>");
    List<String> errors = (List<String>) obj;
    for (String error: errors) {
        out.println("<li>" + error + "</li>");
    }
    out.println("</ul>");
    out.println("</div>");
}
%>
            <div class="post">

                <h1 class="title">examples</h1>
                
                <div id="form">

                    <form action="./jspvalidatingasform" method="post">
                      <label for="firstName">What's the first name?</label>
                      <input name="person.firstName" id="firstName" type="text"/><br/>
                      <label for="lastName">What's the last name?</label>
                      <input name="person.lastName" id="lastName" type="text"/><br/>
                      <label for="age">What's the age?</label>
                      <input name="person.age" id="age" type="text"/><br/>
                      <br/>
                      <input id="submit" type="submit" value="Add a Person via Post"/>
                    </form>
                
                </div>
<!--
                <div id="form2">

                    <form action="./jspvalidatingasform" method="get">
                      <label for="firstName">What's the first name?</label>
                      <input name="person.firstName" id="firstName" type="text"/><br/>
                      <label for="lastName">What's the last name?</label>
                      <input name="person.lastName" id="lastName" type="text"/><br/>
                      <label for="age">What's the age?</label>
                      <input name="person.age" id="age" type="text"/><br/>
                      <br/>
                      <input id="submit" type="submit" value="Add a Person via Get"/>
                    </form>
                
                </div>
-->
                <div class="meta">
                    <p class="byline">Google Sitebricks.</p>
                </div>

            </div>
        </div>
    </div>

</body>
</html>
