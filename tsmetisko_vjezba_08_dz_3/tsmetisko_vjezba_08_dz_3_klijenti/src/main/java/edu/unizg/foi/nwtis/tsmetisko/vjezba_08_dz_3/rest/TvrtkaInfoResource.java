package edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.rest;

import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.GlobalniPodaci;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.ServisTvrtkaKlijent;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.ws.WebSocketTvrtka;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;


/**
 * Klasa TvrtkaResource.
 */
@Path("nwtis/v1/api/tvrtka")
public class TvrtkaInfoResource {

  @Inject
  GlobalniPodaci globalniPodaci;

  @Inject
  @RestClient
  ServisTvrtkaKlijent servisTvrtka;

  /**
   * Head poslužitelj kraj info.
   *
   * @return the response
   */
  @Path("kraj/info")
  @GET
  @Operation(summary = "Informacija o zaustavljanju poslužitelja tvrtka")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Bez sadržaja")})
  @Counted(name = "brojZahtjeva_getPosluziteljKrajInfo",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getPosluziteljKrajInfo", description = "Vrijeme trajanja metode")
  public Response getPosluziteljKrajInfo() {
    int brojObacuna = this.globalniPodaci.getBrojObracuna();
    var radTvrtke = this.servisTvrtka.headPosluzitelj().getStatus();
    if (radTvrtke == 200) {
      WebSocketTvrtka.send("RADI;" + brojObacuna);
    } else {
      WebSocketTvrtka.send("NE RADI;" + brojObacuna);
    }

    return Response.status(Response.Status.OK).build();
  }

  @Path("obracun/ws")
  @GET
  @Operation(summary = "Informacija o zaustavljanju poslužitelja tvrtka")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Bez sadržaja")})
  @Counted(name = "brojZahtjeva_getObracunWs",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getObracunWs", description = "Vrijeme trajanja metode")
  public Response getObracunWs() {
    this.globalniPodaci.povecajBrojObracuna();
    int brojObacuna = this.globalniPodaci.getBrojObracuna();
    var radTvrtke = this.servisTvrtka.headPosluzitelj().getStatus();
    if (radTvrtke == 200) {
      WebSocketTvrtka.send("RADI;" + brojObacuna);
    } else {
      WebSocketTvrtka.send("NE RADI;" + brojObacuna);
    }

    return Response.status(Response.Status.OK).build();
  }

}
