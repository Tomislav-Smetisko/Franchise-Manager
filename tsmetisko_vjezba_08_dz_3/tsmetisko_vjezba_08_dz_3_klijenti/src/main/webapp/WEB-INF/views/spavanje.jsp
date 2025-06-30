<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>REST MVC - Spavanje</title>
        <style type="text/css">
.poruka {
	color: red;
}
        </style>
    </head>
    <body>
        <h1>Spavanje poslužitelja</h1>
       <ul>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna stranica</a>
            </li>
            <li><p>Vrijeme spavanja:</p>        
                <form method="get" action="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/spavanje">
                    <table>
                        <tr>
                            <td>Unesite vrijeme u milisekundama: </td>
                            <td><input name="vrijeme" size="10"/>
                                <input type="hidden" name="${mvc.csrf.name}" value="${mvc.csrf.token}"/>
                            </td>
                            <td><input type="submit" value=" Spavaj "></td>
                        </tr>                      
                    </table>
                </form>
            </li>                     
        </ul>   
    </body>
</html>
