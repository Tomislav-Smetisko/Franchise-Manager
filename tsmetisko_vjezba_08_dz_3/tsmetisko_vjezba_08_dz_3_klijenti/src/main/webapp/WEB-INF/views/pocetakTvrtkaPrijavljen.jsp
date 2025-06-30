<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Vježba 8 - zadaća 3 - Početna stranica</title>
    </head>
    <body>
        <h1>Vježba 8 - zadaća 3 - Početna stranica</h1>
        <ul>
        	<li class="odjava">
                <a href="${pageContext.servletContext.contextPath}/privatno/odjavaKorisnika.xhtml">Odjava</a>
            </li>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna stranica Tvrtka</a>
            </li>
            <li>
                <a href="${pageContext.servletContext.contextPath}/index.xhtml">Početna stranica Partner</a>
            </li>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/kraj">Kraj rada poslužitelja Tvrtka</a>
            </li>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/status">Status poslužitelja Tvrtka</a>
            </li>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/start/1">Start poslužitelja Tvrtka - registracija</a>
            </li>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pauza/1">Pauza poslužitelja Tvrtka - registracija</a>
            </li>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/start/2">Start poslužitelja Tvrtka - za partnere</a>
            </li>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pauza/2">Pauza poslužitelja Tvrtka - za partnere</a>
            </li>
            <li class="admin">
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/nadzornaKonzolaTvrtka">Nadzorna konzola Tvrtka</a>
            </li>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/partner">Pregled svih partnera</a>
            </li>
            <li class="privatno">
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/privatno/obracuni">Pregled obračuna</a>
            </li>
            <li class="admin">
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/prikazDodavanjaPartnera">Dodaj partnera</a>
            </li>
            <li class="admin">
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/spava">Pokreni spavanje</a>
            </li>
            
<%--             <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/korisnici/noviKorisnik">Dodavanje novog korisnika</a>
            </li>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/korisnici/ispisKorisnika">Ispis svih korisnika</a>
            </li>
            <li>
            <h2>Pretraživanje korisnika</h2>
                <form method="post" action="${pageContext.servletContext.contextPath}/mvc/korisnici/pretrazivanjeKorisnika">
                    <table>
                        <tr>
                            <td>Ime: </td>
                            <td><input name="ime"/>
                                <input type="hidden" name="${mvc.csrf.name}" value="${mvc.csrf.token}"/>
                            </td>
                        </tr>
                        <tr>
                            <td>Prezime: </td>
                            <td><input name="prezime"/>
                        </tr>
                        <tr>
                            <td>&nbsp;</td>
                            <td><input type="submit" value=" Dohvati korisnike "></td>
                        </tr>                        
                    </table>
                </form>
            </li>  --%>                    
        </ul>  
        
        <div>
			<script type="text/javascript">
				const prijavljen = ${prijavljen};
			    const admin = ${admin};
				
				if (!prijavljen) {
					document.querySelectorAll(".admin").forEach(el => el.style.display = "none");
					document.querySelectorAll(".privatno").forEach(el => el.style.display = "none");
					document.querySelectorAll(".odjava").forEach(el => el.style.display = "none");
				}
				
				if (prijavljen && !admin) {
					document.querySelectorAll(".admin").forEach(el => el.style.display = "none");
				}
				
				if (prijavljen) {
					document.querySelectorAll(".prijava").forEach(el => el.style.display = "none");
				}
			</script>
		</div>
                
    </body>
</html>
