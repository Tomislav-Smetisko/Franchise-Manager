<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, edu.unizg.foi.nwtis.podaci.Partner" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>REST MVC - Pregled odabranog partnera</title>
        <style type="text/css">
table, th, td {
  border: 1px solid;
}       
th {
	text-align: center;
	font-weight: bold;
} 
.desno {
	text-align: right;
}
        </style>
    </head>
    <body>
        <h1>REST MVC - Pregled odabranog partnera</h1>
       <ul>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna stranica</a>
            </li>
            </ul>
            <br/>       
        <table>
        <tr><th>Korisnik</th><th>Naziv</th><th>Vrsta kuhinje</th><th>Adresa</th><th>Mrežna vrata</th><th>Mrežna vrata za kraj</th><th>GPS Širina</th><th>GPS Dužina</th><th>Sigurnosni kod</th><th>Admin kod</th></tr>
	<%
	Partner p = (Partner) request.getAttribute("partner");
	  %>
       <tr><td><%= p.id() %></td><td><%= p.naziv() %></td><td><%= p.vrstaKuhinje() %></td><td><%= p.adresa() %></td><td><%= p.mreznaVrata() %></td><td><%= p.mreznaVrataKraj() %></td><td><%= p.gpsSirina() %></td><td><%= p.gpsDuzina() %></td><td><%= p.sigurnosniKod() %></td><td><%= p.adminKod() %></td></tr>	  
	  <%
	%>	
        </table>	        
    </body>
</html>
