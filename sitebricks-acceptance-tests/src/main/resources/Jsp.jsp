<%@ page language="java" contentType="text/jsp;charset=UTF-8"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

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
            <div class="post">
                <h1 class="title">examples</h1>
                <div class="entry">

                    The following expression is repeated over every item in the bound collection of names.

                    <ul>
                      <c:forEach var="name" items="${pageFlow.names}">
 		                <li>${index}: ${name}</li>
 		              </c:forEach>
                    </ul>

                    <br/><br/><br/>
                </div>


                <div class="entry">

                    Repeat inside a repeat:

                    <ul>
                      <c:forEach var="movie" items="${pageFlow.movies}">
                        <c:forEach var="actor" items="${movie.actors}">
                           <li>${actor}</li>
                        </c:forEach>
                      </c:forEach>
                    </ul>

                    <br/><br/><br/>
                </div>


                <div class="meta">
                    <p class="byline">Google Sitebricks.</p>
                </div>
            </div>
        </div>
    </div>

</body>
</html>
