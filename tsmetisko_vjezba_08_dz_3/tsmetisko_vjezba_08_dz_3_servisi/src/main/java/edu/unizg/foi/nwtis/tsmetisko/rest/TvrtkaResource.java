package edu.unizg.foi.nwtis.tsmetisko.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import com.google.gson.Gson;
import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import edu.unizg.foi.nwtis.podaci.Obracun;
import edu.unizg.foi.nwtis.podaci.Partner;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.dao.ObracunDAO;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.dao.PartnerDAO;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


/**
 * Klasa TvrtkaResource.
 */
@Path("api/tvrtka")
public class TvrtkaResource {

  /** Tvrtka adresa. */
  @Inject
  @ConfigProperty(name = "adresa")
  private String tvrtkaAdresa;

  /** Mrežna vrata kraj. */
  @Inject
  @ConfigProperty(name = "mreznaVrataKraj")
  private String mreznaVrataKraj;

  /** Mrežna vrata registracija. */
  @Inject
  @ConfigProperty(name = "mreznaVrataRegistracija")
  private String mreznaVrataRegistracija;

  /** Mrežna vrata rad. */
  @Inject
  @ConfigProperty(name = "mreznaVrataRad")
  private String mreznaVrataRad;

  /** Kod za admin tvrtke. */
  @Inject
  @ConfigProperty(name = "kodZaAdminTvrtke")
  private String kodZaAdminTvrtke;

  /** Kod za kraj. */
  @Inject
  @ConfigProperty(name = "kodZaKraj")
  private String kodZaKraj;

  /** Id partner. */
  @Inject
  @ConfigProperty(name = "idPartner")
  private int idPartner;

  @Inject
  @ConfigProperty(name = "klijentTvrtkaInfo/mp-rest/url")
  private String klijentTvrtkaInfo;

  /** Rest konfiguracija. */
  @Inject
  RestConfiguration restConfiguration;

  /**
   * Head poslužitelj.
   *
   * @return the response
   */
  @HEAD
  @Operation(summary = "Provjera statusa poslužitelja tvrtka")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluzitelj", description = "Vrijeme trajanja metode")
  public Response headPosluzitelj() {
    var status = posaljiKomandu("STATUS " + this.kodZaAdminTvrtke + " 2",
        Integer.parseInt(this.mreznaVrataKraj));
    if (status.contains("OK")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Head poslužitelj status.
   *
   * @param id the id
   * @return the response
   */
  @Path("status/{id}")
  @HEAD
  @Operation(summary = "Provjera statusa dijela poslužitelja tvrtka")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Bez sadržaja")})
  @Counted(name = "brojZahtjeva_eadPosluziteljStatus",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_eadPosluziteljStatus", description = "Vrijeme trajanja metode")
  public Response headPosluziteljStatus(@PathParam("id") int id) {
    if (id != 1 && id != 2) {
      return Response.status(Response.Status.NO_CONTENT).build();
    }

    var status = posaljiKomandu("STATUS " + this.kodZaAdminTvrtke + " " + id,
        Integer.parseInt(this.mreznaVrataKraj));
    if (status.equals("OK 1")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  /**
   * Head poslužitelj pauza.
   *
   * @param id the id
   * @return the response
   */
  @Path("pauza/{id}")
  @HEAD
  @Operation(summary = "Postavljanje dijela poslužitelja tvrtka u pauzu")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Bez sadržaja")})
  @Counted(name = "brojZahtjeva_headPosluziteljPauza",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljPauza", description = "Vrijeme trajanja metode")
  public Response headPosluziteljPauza(@PathParam("id") int id) {
    if (id != 1 && id != 2) {
      return Response.status(Response.Status.NO_CONTENT).build();
    }

    var status = posaljiKomandu("PAUZA " + this.kodZaAdminTvrtke + " " + id,
        Integer.parseInt(this.mreznaVrataKraj));
    if (status.equals("OK")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  /**
   * Head poslužitelj start.
   *
   * @param id the id
   * @return the response
   */
  @Path("start/{id}")
  @HEAD
  @Operation(summary = "Postavljanje dijela poslužitelja tvrtka u rad")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Bez sadržaja")})
  @Counted(name = "brojZahtjeva_headPosluziteljStart",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljStart", description = "Vrijeme trajanja metode")
  public Response headPosluziteljStart(@PathParam("id") int id) {
    if (id != 1 && id != 2) {
      return Response.status(Response.Status.NO_CONTENT).build();
    }

    var status = posaljiKomandu("START " + this.kodZaAdminTvrtke + " " + id,
        Integer.parseInt(this.mreznaVrataKraj));
    if (status.equals("OK")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  /**
   * Head poslužitelj kraj.
   *
   * @return the response
   */
  @Path("kraj")
  @HEAD
  @Operation(summary = "Zaustavljanje poslužitelja tvrtka")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Bez sadržaja")})
  @Counted(name = "brojZahtjeva_headPosluziteljKraj",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljKraj", description = "Vrijeme trajanja metode")
  public Response headPosluziteljKraj() {
    var status = posaljiKomandu("KRAJWS " + this.kodZaKraj, Integer.parseInt(this.mreznaVrataKraj));
    if (status.equals("OK")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  /**
   * Head poslužitelj kraj info.
   *
   * @return the response
   */
  @Path("kraj/info")
  @HEAD
  @Operation(summary = "Informacija o zaustavljanju poslužitelja tvrtka")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Bez sadržaja")})
  @Counted(name = "brojZahtjeva_headPosluziteljKrajInfo",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljKrajInfo", description = "Vrijeme trajanja metode")
  public Response headPosluziteljKrajInfo() {
    var odgovor = posaljiNaKrajInfo();
    if (odgovor.contains("OK")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  public String posaljiNaKrajInfo() {
    try {
      String restAdresa = this.klijentTvrtkaInfo + "api/tvrtka/kraj/info";
      HttpClient klijent = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
      HttpRequest zahtjev = HttpRequest.newBuilder().uri(URI.create(restAdresa)).GET().build();

      HttpResponse<Void> odgovor = klijent.send(zahtjev, HttpResponse.BodyHandlers.discarding());
      if (odgovor.statusCode() == 200) {
        return "OK\n";
      } else {
        return "ERROR 17 – RESTful zahtjev nije uspješan\n";
      }
    } catch (Exception e) {
      return "ERROR 17 – RESTful zahtjev nije uspješan\n";
    }
  }

  /**
   * Get jelovnici.
   *
   * @return the jelovnici
   */
  @Path("jelovnik")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat svih jelovnika")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getJelovnik",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getJelovnik", description = "Vrijeme trajanja metode")
  public Response getJelovnici() {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      List<Partner> partneri = partnerDAO.dohvatiSve(false);

      Map<String, List<Jelovnik>> response = new HashMap<>();
      Gson gson = new Gson();
      for (var partner : partneri) {
        String komanda = "JELOVNIK " + partner.id() + " " + partner.sigurnosniKod();
        var odgovor = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataRad));

        if (!odgovor.isEmpty() && !odgovor.contains("ERROR")) {
          Jelovnik[] jelovnikNiz = gson.fromJson(odgovor, Jelovnik[].class);
          List<Jelovnik> jelovnikPopisJson = Arrays.asList(jelovnikNiz);
          response.putIfAbsent(jelovnikPopisJson.get(0).id().substring(0, 2), jelovnikPopisJson);
        }
      }

      if (!response.isEmpty()) {
        return Response.ok(response).status(Response.Status.OK).build();
      } else {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get jelovnici id.
   *
   * @param id the id
   * @return the jelovnici
   */
  @Path("jelovnik/{id}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat jelovnika odabranog partnera")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška"),
      @APIResponse(responseCode = "404", description = "Partner nije pronađen")})
  @Counted(name = "brojZahtjeva_getJelovnik",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getJelovnik", description = "Vrijeme trajanja metode")
  public Response getJelovnici(@PathParam("id") int id) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      Partner partner = partnerDAO.dohvati(id, false);

      if (partner == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }

      String komanda = "JELOVNIK " + partner.id() + " " + partner.sigurnosniKod();
      var odgovor = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataRad));

      List<Jelovnik> jelovnikPopisJson = null;
      if (!odgovor.contains("ERROR")) {
        Gson gson = new Gson();
        Jelovnik[] jelovnikNiz = gson.fromJson(odgovor, Jelovnik[].class);
        jelovnikPopisJson = Arrays.asList(jelovnikNiz);
      }

      if (!jelovnikPopisJson.isEmpty()) {
        return Response.ok(jelovnikPopisJson).status(Response.Status.OK).build();
      } else {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get karta pica.
   *
   * @return the karta pica
   */
  @Path("kartapica")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat karte pica")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getKartaPica",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getKartaPica", description = "Vrijeme trajanja metode")
  public Response getKartaPica() {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      Partner partner = partnerDAO.dohvati(this.idPartner, false);

      if (partner == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }

      String komanda = "KARTAPIĆA " + partner.id() + " " + partner.sigurnosniKod();
      var odgovor = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataRad));

      List<KartaPica> kartaPicaJson = null;
      if (!odgovor.contains("ERROR")) {
        Gson gson = new Gson();
        KartaPica[] kartaPicaNiz = gson.fromJson(odgovor, KartaPica[].class);
        kartaPicaJson = Arrays.asList(kartaPicaNiz);
      }

      if (kartaPicaJson != null) {
        return Response.ok(kartaPicaJson).status(Response.Status.OK).build();
      } else {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Gets partneri.
   *
   * @return the partneri
   */
  @Path("partner")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat svih partnera")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getPartneri",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getPartneri", description = "Vrijeme trajanja metode")
  public Response getPartneri() {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      var partneri = partnerDAO.dohvatiSve(false);

      if (partneri != null) {
        return Response.ok(partneri).status(Response.Status.OK).build();
      } else {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get partner provjera.
   *
   * @return the partner provjera
   */
  @Path("partner/provjera")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Provjera partnera")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getPartnerProvjera",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getPartnerProvjera", description = "Vrijeme trajanja metode")
  public Response getPartnerProvjera() {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      List<Partner> partneriBaza = partnerDAO.dohvatiSve(false);

      String partneriPopis =
          posaljiKomandu("POPIS", Integer.parseInt(this.mreznaVrataRegistracija));
      Gson gson = new Gson();
      Partner[] partneriNiz = gson.fromJson(partneriPopis, Partner[].class);
      List<Partner> partneriPopisJson = Arrays.asList(partneriNiz);

      List<Partner> listaPodudarajućih = new ArrayList<>();
      for (var partner : partneriPopisJson) {
        for (var partnerUBazi : partneriBaza) {
          if (partner.id() == partnerUBazi.id() && partner.naziv().equals(partnerUBazi.naziv())
              && partner.vrstaKuhinje().equals(partnerUBazi.vrstaKuhinje())
              && partner.adresa().equals(partnerUBazi.adresa())
              && partner.mreznaVrata() == partnerUBazi.mreznaVrata()
              && partner.gpsSirina() == partnerUBazi.gpsSirina()
              && partner.gpsDuzina() == partnerUBazi.gpsDuzina()) {
            listaPodudarajućih.add(partner);
          }
        }
      }

      if (listaPodudarajućih != null) {
        return Response.ok(listaPodudarajućih).status(Response.Status.OK).build();
      } else {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get partner id.
   *
   * @param id the id
   * @return the partner
   */
  @Path("partner/{id}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat jednog partnera")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "404", description = "Ne postoji resurs"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getPartner",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getPartner", description = "Vrijeme trajanja metode")
  public Response getPartner(@PathParam("id") int id) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      var partner = partnerDAO.dohvati(id, true);
      if (partner != null) {
        return Response.ok(partner).status(Response.Status.OK).build();
      } else {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Post partner.
   *
   * @param partner the partner
   * @return the response
   */
  @Path("partner")
  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dodavanje jednog partnera")
  @APIResponses(
      value = {@APIResponse(responseCode = "201", description = "Uspješna kreiran resurs"),
          @APIResponse(responseCode = "409", description = "Već postoji resurs ili druga pogreška"),
          @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_postPartner",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_postPartner", description = "Vrijeme trajanja metode")
  public Response postPartner(Partner partner) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      var status = partnerDAO.dodaj(partner);
      if (status) {
        return Response.status(Response.Status.CREATED).build();
      } else {
        return Response.status(Response.Status.CONFLICT).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get obracun.
   *
   * @param vrijemeOd the vrijeme od
   * @param vrijemeDo the vrijeme do
   * @return the obracun
   */
  @Path("obracun")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat obračuna")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getObracun",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getObracun", description = "Vrijeme trajanja metode")
  public Response getObracun(@QueryParam("od") Long vrijemeOd, @QueryParam("do") Long vrijemeDo) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var obracunDAO = new ObracunDAO(vezaBP);
      List<Obracun> obracuni = obracunDAO.dohvati(vrijemeOd, vrijemeDo);
      if (obracuni != null) {
        return Response.ok(obracuni).status(Response.Status.OK).build();
      } else {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get obracun jelo.
   *
   * @param vrijemeOd the vrijeme od
   * @param vrijemeDo the vrijeme do
   * @return the obracun jelo
   */
  @Path("obracun/jelo")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat obračuna s jelima")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getObracunJelo",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getObracunJelo", description = "Vrijeme trajanja metode")
  public Response getObracunJelo(@QueryParam("od") Long vrijemeOd,
      @QueryParam("do") Long vrijemeDo) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var obracunDAO = new ObracunDAO(vezaBP);
      List<Obracun> obracuni = obracunDAO.dohvatiJela(vrijemeOd, vrijemeDo);
      if (obracuni != null) {
        return Response.ok(obracuni).status(Response.Status.OK).build();
      } else {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get obracun pice.
   *
   * @param vrijemeOd the vrijeme od
   * @param vrijemeDo the vrijeme do
   * @return the obracun pice
   */
  @Path("obracun/pice")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat obračuna s piićima")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getObracunPice",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getObracunPice", description = "Vrijeme trajanja metode")
  public Response getObracunPice(@QueryParam("od") Long vrijemeOd,
      @QueryParam("do") Long vrijemeDo) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var obracunDAO = new ObracunDAO(vezaBP);
      List<Obracun> obracuni = obracunDAO.dohvatiPica(vrijemeOd, vrijemeDo);
      if (obracuni != null) {
        return Response.ok(obracuni).status(Response.Status.OK).build();
      } else {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get obracun partner.
   *
   * @param id the id
   * @param vrijemeOd the vrijeme od
   * @param vrijemeDo the vrijeme do
   * @return the obracun partner
   */
  @Path("obracun/{id}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat obračuna za partnera")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getObracunPartner",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getObracunPartner", description = "Vrijeme trajanja metode")
  public Response getObracunPartner(@PathParam("id") int id, @QueryParam("od") Long vrijemeOd,
      @QueryParam("do") Long vrijemeDo) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var obracunDAO = new ObracunDAO(vezaBP);
      List<Obracun> obracuni = obracunDAO.dohvatiZaPartnera(id, vrijemeOd, vrijemeDo);
      if (obracuni != null) {
        return Response.ok(obracuni).status(Response.Status.OK).build();
      } else {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Post obracun.
   *
   * @param listaObracuna the lista obracuna
   * @return the response
   */
  @Path("obracun")
  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dodavanje obračuna")
  @APIResponses(
      value = {@APIResponse(responseCode = "201", description = "Uspješna kreiran resurs"),
          @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_postObracun",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_postObracun", description = "Vrijeme trajanja metode")
  public Response postObracun(List<Obracun> listaObracuna) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var obracunDAO = new ObracunDAO(vezaBP);
      var status = obracunDAO.dodajObracune(listaObracuna);

      if (status) {
        var odgovor = posaljiNaObracunWs();
        if (odgovor.contains("OK")) {
          return Response.status(Response.Status.CREATED).build();
        } else {
          return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
      } else {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  public String posaljiNaObracunWs() {
    try {
      String restAdresa = this.klijentTvrtkaInfo + "api/tvrtka/obracun/ws";
      HttpClient klijent = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
      HttpRequest zahtjev = HttpRequest.newBuilder().uri(URI.create(restAdresa)).GET().build();

      HttpResponse<Void> odgovor = klijent.send(zahtjev, HttpResponse.BodyHandlers.discarding());
      if (odgovor.statusCode() == 200) {
        return "OK\n";
      } else {
        return "ERROR 17 – RESTful zahtjev nije uspješan\n";
      }
    } catch (Exception e) {
      return "ERROR 17 – RESTful zahtjev nije uspješan\n";
    }
  }

  /**
   * Post obracun ws.
   *
   * @param listaObracuna the lista obracuna
   * @return the response
   */
  @Path("obracun/ws")
  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dodavanje obračuna ws")
  @APIResponses(
      value = {@APIResponse(responseCode = "201", description = "Uspješna kreiran resurs"),
          @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_postObracunWs",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_postObracunWs", description = "Vrijeme trajanja metode")
  public Response postObracunWs(List<Obracun> listaObracuna) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      Partner partner = partnerDAO.dohvati(this.idPartner, false);

      Gson gson = new Gson();
      String json = gson.toJson(listaObracuna);
      String komanda = "OBRAČUNWS " + this.idPartner + " " + partner.sigurnosniKod() + "\n" + json;
      String odgovor = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataRad));

      if (odgovor.contains("ERROR")) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }

      var obracunDAO = new ObracunDAO(vezaBP);
      var status = obracunDAO.dodajObracune(listaObracuna);

      if (status && odgovor.equals("OK")) {
        return Response.status(Response.Status.CREATED).build();
      } else {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get spava.
   *
   * @param vrijeme the vrijeme
   * @return the spava
   */
  @Path("spava")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Spavanje poslužitelja")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getSpava", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getSpava", description = "Vrijeme trajanja metode")
  public Response getSpava(@QueryParam("vrijeme") long vrijeme) {
    String komanda = "SPAVA " + this.kodZaAdminTvrtke + " " + vrijeme;
    String odgovor = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataKraj));

    if (odgovor.equals("OK")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Posalji komandu.
   *
   * @param komanda the komanda
   * @param mreznaVrata the mrezna vrata
   * @return the string
   */
  private String posaljiKomandu(String komanda, int mreznaVrata) {
    try {
      Socket mreznaUticnica = new Socket();
      mreznaUticnica.connect(new InetSocketAddress(this.tvrtkaAdresa, mreznaVrata), 2000);

      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
      out.write(komanda + "\n");
      out.flush();
      mreznaUticnica.shutdownOutput();
      var linija = in.readLine();

      if (komanda.contains("JELOVNIK") || komanda.contains("KARTAPIĆA")
          || komanda.equals("POPIS")) {
        StringBuilder sb = new StringBuilder();
        while ((linija = in.readLine()) != null) {
          sb.append(linija).append("\n");
          if (linija.isBlank() || linija.trim().endsWith("]") || !in.ready()) {
            break;
          }
        }
        linija = sb.toString();
      }

      mreznaUticnica.shutdownInput();
      mreznaUticnica.close();
      return linija;
    } catch (IOException e) {
      return "ERROR";
    }
    // return null;
  }
}
