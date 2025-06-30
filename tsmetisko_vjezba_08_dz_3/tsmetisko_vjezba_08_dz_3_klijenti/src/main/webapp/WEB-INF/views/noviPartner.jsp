<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>REST MVC - Dodavanje partnera</title>
        <style type="text/css">
.poruka {
	color: red;
}
        </style>
    </head>
    <body>
        <h1>Dodavanje partnera</h1>
       <ul>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna stranica</a>
            </li>
            <%
            if(request.getAttribute("poruka") != null) {
              String poruka = (String) request.getAttribute("poruka");
              Object oPogreska = request.getAttribute("pogreska");
              boolean pogreska = false;
              System.out.println(oPogreska);
              if(oPogreska != null) {
                pogreska = (Boolean) oPogreska;
              }
              if(poruka.length() > 0) {
                String klasa = "";
                if(pogreska) {
                  klasa = "poruka";
                }
                %>
                <li>
                <p class="<%= klasa%>">${poruka}</p>
                </li>
                <%
              }
            }
            %>  
            <li><p>Podaci partnera:</p>          
                <form method="post" action="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/dodajPartnera">
                    <table>
                        <tr>
                            <td>Id: </td>
                            <td><input name="id" size="10"/>
                                <input type="hidden" name="${mvc.csrf.name}" value="${mvc.csrf.token}"/>
                            </td>
                        </tr>
                        <tr>
                            <td>Naziv: </td>
                            <td><input name="naziv" size="30"/>
                            </td>
                        </tr>
                        <tr>
                            <td>Vrsta kuhinje: </td>
                            <td><input name="vk" size="10"/></td>
                        </tr>
                        <tr>
                            <td>Adresa: </td>
                            <td><input name="adresa" size="20"/>
                            </td>
                        </tr>
                        <tr>
                            <td>Mrežna vrata: </td>
                            <td><input name="mv" size="20"/></td>
                        </tr>
                        <tr>
                            <td>Mrežna vrata kraj: </td>
                            <td><input name="mvk" size="20"/></td>
                        </tr>
                        <tr>
                            <td>GPS Širina: </td>
                            <td><input name="gpsSirina" size="20"/></td>
                        </tr>
                        <tr>
                            <td>GPS Dužina: </td>
                            <td><input name="gpsDuzina" size="20"/></td>
                        </tr>
                        <tr>
                            <td>Sigurnosni kod: </td>
                            <td><input name="sigurnosniKod" size="20"/></td>
                        </tr>
                        <tr>
                            <td>Admin kod: </td>
                            <td><input name="adminKod" size="20"/></td>
                        </tr>
                        <tr>
                            <td>&nbsp;</td>
                            <td><input type="submit" value=" Dodaj partnera "></td>
                        </tr>                        
                    </table>
                </form>
            </li>                     
        </ul>   
    </body>
</html>
