<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, edu.unizg.foi.nwtis.podaci.Obracun"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>REST MVC - Pregled obračuna</title>
<style type="text/css">
table, th, td {
	border: 1px solid;
}

th {
	text-align: center;
	font-weight: bold;
}

.forma {
	display: flex;
	gap: 40px;
	align-items: flex-start;
}
</style>
</head>
<body>
	<h1>REST MVC - Pregled obračuna</h1>
	<ul>
		<li><a
			href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna
				stranica</a></li>
	</ul>
	<br />

	<div class="forma">
		<form method="get"
			action="${pageContext.servletContext.contextPath}/mvc/tvrtka/privatno/obracuni">
			<br><br>
			<table>
				<tr>
					<td>Vrijeme od:</td>
					<td><input type="datetime-local" name="od" /> <input
						type="hidden" name="${mvc.csrf.name}" value="${mvc.csrf.token}" />
					</td>
				</tr>
				<tr>
					<td>Vrijeme do:</td>
					<td><input type="datetime-local" name="do" />
				</tr>
				<tr>
					<td>Tip obračuna</td>
					<td><input type="radio" id="jeloPice" name="vrsta"
						value="jeloPice" checked> <label for="jeloPice">Jelo
							i piće</label><br /> <input type="radio" id="jelo" name="vrsta"
						value="jelo"> <label for="jelo">Jelo</label><br /> <input
						type="radio" id="pice" name="vrsta" value="pice"> <label
						for="pice">Piće</label></td>
				</tr>
				<tr>
					<td>&nbsp;</td>
					<td><input type="submit" value=" Dohvati obračune "></td>
				</tr>
			</table>
		</form>

		<form method="get"
			action="${pageContext.servletContext.contextPath}/mvc/tvrtka/privatno/obracuni/partner">
			<h2>Pregled obračuna partnera</h2>
			<table>
				<tr>
					<td>Id partnera</td>
					<td><input name="id" /> <input type="hidden"
						name="${mvc.csrf.name}" value="${mvc.csrf.token}" />
				</tr>
				<tr>
					<td>Vrijeme od:</td>
					<td><input type="datetime-local" name="od" /></td>
				</tr>
				<tr>
					<td>Vrijeme do:</td>
					<td><input type="datetime-local" name="do" />
				</tr>
				<tr>
					<td>&nbsp;</td>
					<td><input type="submit" value=" Dohvati obračune "></td>
				</tr>
			</table>
		</form>
	</div>
	<br />
	<table>
		<tr>
			<th>R. br.</th>
			<th>Partner</th>
			<th>Id</th>
			<th>Jelo</th>
			<th>Količina</th>
			<th>Cijena</th>
			<th>Vrijeme</th>
		</tr>
		<%
		List<Obracun> obracuni = (List<Obracun>) request.getAttribute("obracuni");
		int i = 0;
		for (Obracun o : obracuni) {
		  i++;
		%>
		<tr>
			<td><%=i%></td>
			<td><%=o.partner()%></td>
			<td><%=o.id()%></td>
			<td><%=o.jelo()%></td>
			<td><%=o.kolicina()%></td>
			<td><%=o.cijena()%></td>
			<td><%=o.vrijeme()%></td>
		</tr>
		<%
		}
		%>
	</table>
</body>
</html>
