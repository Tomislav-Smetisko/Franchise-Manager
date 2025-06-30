<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Vježba 8 - zadaća 3 - Nadzorna konzola tvrtka</title>

<style type="text/css">
table, th, td {
	border: 1px solid;
}

th {
	text-align: center;
	font-weight: bold;
}
</style>
</head>

<body>
	<h1>Vježba 8 - zadaća 3 - Nadzorna konzola tvrtka</h1>
	<ul>
		<li><a
			href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna
				stranica Tvrtka</a></li>
		<li><a
			href="${pageContext.servletContext.contextPath}/index.xhtml">Početna
				stranica Partner</a></li>
	</ul>
	<br />

	<h2>Statusi dijelova poslužitelja</h2>
	<table>
		<tr>
			<th>Dio poslužitelja</th>
			<th>Status</th>
		</tr>
		<tr>
			<td>Status poslužitelja za registraciju</td>
			<td><%=request.getAttribute("statusT1")%></td>
			<td>
				<form method="get"
					action="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/pauzaKonzola/1">
					<input type="submit" value="Pauziraj">
				</form>
			</td>
			<td>
				<form method="get"
					action="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/startKonzola/1">
					<input type="submit" value="Pokreni">
				</form>
			</td>
		</tr>
		<tr>
			<td>Status poslužitelja za partnere</td>
			<td><%=request.getAttribute("statusT2")%></td>
			<td>
				<form method="get"
					action="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/pauzaKonzola/2">
					<input type="submit" value="Pauziraj">
				</form>
			</td>
			<td>
				<form method="get"
					action="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/startKonzola/2">
					<input type="submit" value="Pokreni">
				</form>
			</td>
		</tr>
		<tr>
			<td>Status poslužitelja</td>
			<td><%=request.getAttribute("statusT")%></td>
			<td colspan="2">
				<form method="get"
					action="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/krajKonzola">
					<input type="submit" value="Zaustavi rad poslužitelja Tvrtka">
				</form>
			</td>
		</tr>
	</table>

	<h2>Informacije</h2>
	<div>
		<p>
			Status rada poslužitelja Tvrtka: <span id="statusRada"></span>
		</p>
	</div>
	<div>
		<p>
			Broj primljenih obračuna: <span id="brojObracuna"></span>
		</p>
	</div>

	<div>
		<p>
			Poruka: <span id="poruka"></span>
		</p>
	</div>

	<div>
		<h3>Pošalji internu poruku</h3>
		<form id="forma" onsubmit="posaljiPoruku(); return false;">
			<textarea id="porukaZaSlanje" rows="4" cols="50" placeholder="Upiši poruku..."
				required></textarea> <input type="submit" value="Pošalji" />
		</form>
	</div>

	<div>
		<script type="text/javascript">
			var wsocket;
			function connect() {
				var adresa = window.location.pathname;
				var dijelovi = adresa.split("/");
				adresa = "ws://" + window.location.hostname + ":"
						+ window.location.port + "/" + dijelovi[1]
						+ "/ws/tvrtka";
				if ('WebSocket' in window) {
					wsocket = new WebSocket(adresa);
				} else if ('MozWebSocket' in window) {
					wsocket = new MozWebSocket(adresa);
				} else {
					alert('WebSocket nije podržan od web preglednika.');
					return;
				}
				wsocket.onmessage = onMessage;
			}

			function onMessage(evt) {
				var poruka = evt.data.split(";");
				
				if (poruka.length == 2) {
					document.getElementById("statusRada").innerHTML = poruka[0];
					if (poruka[0] === "RADI") {
						document.getElementById("statusRada").style.color = "green";
					} else {
						document.getElementById("statusRada").style.color = "red";
					}
					document.getElementById("brojObracuna").innerHTML = poruka[1];
				} else if (poruka.length == 3) {
					document.getElementById("statusRada").innerHTML = poruka[0];
					if (poruka[0] === "RADI") {
						document.getElementById("statusRada").style.color = "green";
					} else {
						document.getElementById("statusRada").style.color = "red";
					}

					document.getElementById("brojObracuna").innerHTML = poruka[1];
					var porukaElem = document.getElementById("poruka");
					porukaElem.innerHTML = poruka[2];
				} else {
					var porukaElem = document.getElementById("poruka");
					porukaElem.innerHTML = poruka;
				}
			}

			function posaljiPoruku() {
				var porukaZaSlanje = document.getElementById("porukaZaSlanje").value
						.trim();
				wsocket.send(porukaZaSlanje);
				document.getElementById("porukaZaSlanje").value = "";
			}

			window.addEventListener("load", connect, false);
		</script>
	</div>
</body>
</html>
