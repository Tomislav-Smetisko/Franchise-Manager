package edu.unizg.foi.nwtis.tsmetisko.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import com.google.gson.Gson;
import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.podaci.Narudzba;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.dao.KorisnikDAO;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;



/**
 * Klasa PartnerResource.
 */
@Path("api/partner")
public class PartnerResource {

  /** Adresa partner. */
  @Inject
  @ConfigProperty(name = "adresaPartner")
  private String adresaPartner;

  /** Mrežna vrata kraj partner. */
  @Inject
  @ConfigProperty(name = "mreznaVrataKrajPartner")
  private String mreznaVrataKrajPartner;

  /** Mrežna vrata rad partner. */
  @Inject
  @ConfigProperty(name = "mreznaVrataRadPartner")
  private String mreznaVrataRadPartner;

  /** Kod za admin partnera. */
  @Inject
  @ConfigProperty(name = "kodZaAdminPartnera")
  private String kodZaAdminPartnera;

  /** Kod za kraj. */
  @Inject
  @ConfigProperty(name = "kodZaKraj")
  private String kodZaKraj;

  /** Rest konfiguracija. */
  @Inject
  RestConfiguration restConfiguration;


  /**
   * Head poslužitelj.
   *
   * @return the response
   */
  @HEAD
  @Operation(summary = "Provjera da li radi poslužitelj partner")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluzitelj", description = "Vrijeme trajanja metode")
  public Response headPosluzitelj() {
    String komanda = "STATUS " + this.kodZaAdminPartnera + " 1";
    var status = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataKrajPartner));

    if (status.contains("OK")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Head posluzitelj status.
   *
   * @param id the id
   * @return the response
   */
  @Path("status/{id}")
  @HEAD
  @Operation(summary = "Provjera statusa poslužitelja partner")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Bez sadržaja")})
  @Counted(name = "brojZahtjeva_", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljStatus", description = "Vrijeme trajanja metode")
  public Response headPosluziteljStatus(@PathParam("id") int id) {
    if (id != 1) {
      return Response.status(Response.Status.NO_CONTENT).build();
    }

    String komanda = "STATUS " + this.kodZaAdminPartnera + " " + id;
    var status = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataKrajPartner));

    if (status.equals("OK 1")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  /**
   * Head pauza.
   *
   * @param id the id
   * @return the response
   */
  @Path("pauza/{id}")
  @HEAD
  @Operation(summary = "Pauziranje određenog dijela poslužitelj partner")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Bez sadržaja")})
  @Counted(name = "brojZahtjeva_", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPauza", description = "Vrijeme trajanja metode")
  public Response headPauza(@PathParam("id") int id) {
    if (id != 1) {
      return Response.status(Response.Status.NO_CONTENT).build();
    }

    String komanda = "PAUZA " + this.kodZaAdminPartnera + " " + id;
    var status = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataKrajPartner));

    if (status.contains("OK")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  /**
   * Head start.
   *
   * @param id the id
   * @return the response
   */
  @Path("start/{id}")
  @HEAD
  @Operation(summary = "Startanje određenog dijela poslužitelj partner")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Bez sadržaja")})
  @Counted(name = "brojZahtjeva_", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headStart", description = "Vrijeme trajanja metode")
  public Response headStart(@PathParam("id") int id) {
    if (id != 1) {
      return Response.status(Response.Status.NO_CONTENT).build();
    }

    String komanda = "START " + this.kodZaAdminPartnera + " " + id;
    var status = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataKrajPartner));

    if (status.contains("OK")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  /**
   * Head kraj.
   *
   * @return the response
   */
  @Path("kraj")
  @HEAD
  @Operation(summary = "Kraj svih dijelova poslužitelj partner")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Bez sadržaja")})
  @Counted(name = "brojZahtjeva_", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headKraj", description = "Vrijeme trajanja metode")
  public Response headKraj() {
    String komanda = "KRAJ " + this.kodZaKraj;
    var status = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataKrajPartner));

    if (status.contains("OK")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  /**
   * Get jelovnik.
   *
   * @param korisnik the korisnik
   * @param lozinka the lozinka
   * @return the jelovnik
   */
  @Path("jelovnik")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat jelovnika korisnika")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "401", description = "Ne autoriziran"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getJelovnik",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getJelovnik", description = "Vrijeme trajanja metode")
  public Response getJelovnik(@HeaderParam("korisnik") String korisnik,
      @HeaderParam("lozinka") String lozinka) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      Korisnik k = korisnikDAO.dohvati(korisnik, lozinka, true);

      if (k == null) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }

      String odgovor =
          posaljiKomandu("JELOVNIK " + korisnik, Integer.parseInt(this.mreznaVrataRadPartner));

      List<Jelovnik> jelovnikPopisJson = null;
      if (!odgovor.contains("ERROR")) {
        Gson gson = new Gson();
        Jelovnik[] jelovnikNiz = gson.fromJson(odgovor, Jelovnik[].class);
        jelovnikPopisJson = Arrays.asList(jelovnikNiz);
      }

      if (!jelovnikPopisJson.isEmpty()) {
        return Response.ok(jelovnikPopisJson).status(Response.Status.OK).build();
      } else {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }

    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get karta pica.
   *
   * @param korisnik the korisnik
   * @param lozinka the lozinka
   * @return the karta pica
   */
  @Path("kartapica")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat kartu pića")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "401", description = "Ne autoriziran"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getKartaPica",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getKartaPica", description = "Vrijeme trajanja metode")
  public Response getKartaPica(@HeaderParam("korisnik") String korisnik,
      @HeaderParam("lozinka") String lozinka) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      Korisnik k = korisnikDAO.dohvati(korisnik, lozinka, true);

      if (k == null) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }

      String odgovor =
          posaljiKomandu("KARTAPIĆA " + korisnik, Integer.parseInt(this.mreznaVrataRadPartner));

      List<KartaPica> kartaPicaPopisJson = null;
      if (!odgovor.contains("ERROR")) {
        Gson gson = new Gson();
        KartaPica[] kartaPicaNiz = gson.fromJson(odgovor, KartaPica[].class);
        kartaPicaPopisJson = Arrays.asList(kartaPicaNiz);
      }

      if (!kartaPicaPopisJson.isEmpty()) {
        return Response.ok(kartaPicaPopisJson).status(Response.Status.OK).build();
      } else {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }

    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get narudzba.
   *
   * @param korisnik the korisnik
   * @param lozinka the lozinka
   * @return the narudzba
   */
  @Path("narudzba")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat otvorenih narudžbi korisnika")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "401", description = "Ne autoriziran"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getNarudzba",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getNarudzba", description = "Vrijeme trajanja metode")
  public Response getNarudzba(@HeaderParam("korisnik") String korisnik,
      @HeaderParam("lozinka") String lozinka) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      Korisnik k = korisnikDAO.dohvati(korisnik, lozinka, true);

      if (k == null) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }

      String odgovor =
          posaljiKomandu("STANJE " + korisnik, Integer.parseInt(this.mreznaVrataRadPartner));

      List<Narudzba> narudzbaPopisJson = null;
      if (!odgovor.contains("ERROR") && !odgovor.equals("[]")) {
        Gson gson = new Gson();
        Narudzba[] narudzbaNiz = gson.fromJson(odgovor, Narudzba[].class);
        narudzbaPopisJson = Arrays.asList(narudzbaNiz);
      }

      // if (!narudzbaPopisJson.isEmpty()) {
      return Response.ok(narudzbaPopisJson).status(Response.Status.OK).build();
      // } else {
      // return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      // }

    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Post narudzba.
   *
   * @param korisnik the korisnik
   * @param lozinka the lozinka
   * @return the response
   */
  @Path("narudzba")
  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dodavanje narudžbe")
  @APIResponses(
      value = {@APIResponse(responseCode = "201", description = "Uspješna kreiran resurs"),
          @APIResponse(responseCode = "401", description = "Ne autoriziran"),
          @APIResponse(responseCode = "409", description = "Pogrešna operacija"),
          @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_postNarudzba",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_postNarudzba", description = "Vrijeme trajanja metode")
  public Response postNarudzba(@HeaderParam("korisnik") String korisnik,
      @HeaderParam("lozinka") String lozinka) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      Korisnik k = korisnikDAO.dohvati(korisnik, lozinka, true);

      if (k == null) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }

      String odgovor =
          posaljiKomandu("NARUDŽBA " + korisnik, Integer.parseInt(this.mreznaVrataRadPartner));
      if (odgovor.equals("OK")) {
        return Response.status(Response.Status.CREATED).build();
      } else {
        return Response.status(Response.Status.CONFLICT).build();
      }

    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Post jelo.
   *
   * @param korisnik the korisnik
   * @param lozinka the lozinka
   * @param narudzba the narudzba
   * @return the response
   */
  @Path("jelo")
  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dodavanje jela u narudžbu")
  @APIResponses(
      value = {@APIResponse(responseCode = "201", description = "Uspješna kreiran resurs"),
          @APIResponse(responseCode = "401", description = "Ne autoriziran"),
          @APIResponse(responseCode = "409", description = "Pogrešna operacija"),
          @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_postJelo", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_postJelo", description = "Vrijeme trajanja metode")
  public Response postJelo(@HeaderParam("korisnik") String korisnik,
      @HeaderParam("lozinka") String lozinka, Narudzba narudzba) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      Korisnik k = korisnikDAO.dohvati(korisnik, lozinka, true);

      if (k == null) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }
      if (!narudzba.jelo()) {
        return Response.status(Response.Status.CONFLICT).build();
      }

      String komanda =
          "JELO " + narudzba.korisnik() + " " + narudzba.id() + " " + narudzba.kolicina();
      String odgovor = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataRadPartner));
      if (odgovor.equals("OK")) {
        return Response.status(Response.Status.CREATED).build();
      } else {
        return Response.status(Response.Status.CONFLICT).build();
      }

    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Post pice.
   *
   * @param korisnik the korisnik
   * @param lozinka the lozinka
   * @param narudzba the narudzba
   * @return the response
   */
  @Path("pice")
  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dodavanje pića u narudžbu")
  @APIResponses(
      value = {@APIResponse(responseCode = "201", description = "Uspješna kreiran resurs"),
          @APIResponse(responseCode = "401", description = "Ne autoriziran"),
          @APIResponse(responseCode = "409", description = "Pogrešna operacija"),
          @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_postPice", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_postPice", description = "Vrijeme trajanja metode")
  public Response postPice(@HeaderParam("korisnik") String korisnik,
      @HeaderParam("lozinka") String lozinka, Narudzba narudzba) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      Korisnik k = korisnikDAO.dohvati(korisnik, lozinka, true);

      if (k == null) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }
      if (narudzba.jelo()) {
        return Response.status(Response.Status.CONFLICT).build();
      }

      String komanda =
          "PIĆE " + narudzba.korisnik() + " " + narudzba.id() + " " + narudzba.kolicina();
      String odgovor = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataRadPartner));
      if (odgovor.equals("OK")) {
        return Response.status(Response.Status.CREATED).build();
      } else {
        return Response.status(Response.Status.CONFLICT).build();
      }

    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Post racun.
   *
   * @param korisnik the korisnik
   * @param lozinka the lozinka
   * @return the response
   */
  @Path("racun")
  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Izdavanje računa")
  @APIResponses(
      value = {@APIResponse(responseCode = "201", description = "Uspješna kreiran resurs"),
          @APIResponse(responseCode = "401", description = "Ne autoriziran"),
          @APIResponse(responseCode = "409", description = "Pogrešna operacija"),
          @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_postRacun",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_postRacun", description = "Vrijeme trajanja metode")
  public Response postRacun(@HeaderParam("korisnik") String korisnik,
      @HeaderParam("lozinka") String lozinka) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      Korisnik k = korisnikDAO.dohvati(korisnik, lozinka, true);

      if (k == null) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }

      String odgovor =
          posaljiKomandu("RAČUN " + korisnik, Integer.parseInt(this.mreznaVrataRadPartner));
      if (odgovor.equals("OK")) {
        return Response.status(Response.Status.CREATED).build();
      } else {
        return Response.status(Response.Status.CONFLICT).build();
      }

    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get korisnik.
   *
   * @return the korisnik
   */
  @Path("korisnik")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat svih korisnika")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getKorisnik",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getKorisnik", description = "Vrijeme trajanja metode")
  public Response getKorisnik() {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      List<Korisnik> sviKorisnici = korisnikDAO.dohvatiSve();

      if (sviKorisnici == null) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }

      return Response.ok(sviKorisnici).status(Response.Status.OK).build();

    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get korisnik id.
   *
   * @param id the id
   * @return the korisnik id
   */
  @Path("korisnik/{id}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat odabranog korisnika")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "404", description = "Partner nije pronađen"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getKorisnikId",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getKorisnikId", description = "Vrijeme trajanja metode")
  public Response getKorisnikId(@PathParam("id") String id) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      Korisnik korisnik = korisnikDAO.dohvati(id, null, false);

      if (korisnik == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }

      return Response.ok(korisnik).status(Response.Status.OK).build();

    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Post korisnik.
   *
   * @param korisnik the korisnik
   * @return the response
   */
  @Path("korisnik")
  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dodavanje novog korisnika")
  @APIResponses(
      value = {@APIResponse(responseCode = "201", description = "Uspješna kreiran resurs"),
          @APIResponse(responseCode = "409", description = "Pogrešna operacija"),
          @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_postKorisnik",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_postKorisnik", description = "Vrijeme trajanja metode")
  public Response postKorisnik(Korisnik korisnik) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      boolean status = korisnikDAO.dodaj(korisnik);

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
   * Get spava.
   *
   * @param vrijeme the vrijeme
   * @return the spava
   */
  @Path("spava")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Spavanje")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getSpava", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getSpava", description = "Vrijeme trajanja metode")
  public Response getSpava(@QueryParam("vrijeme") long vrijeme) {
    String odgovor = posaljiKomandu("SPAVA " + this.kodZaAdminPartnera + " " + vrijeme,
        Integer.parseInt(this.mreznaVrataKrajPartner));
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
      mreznaUticnica.connect(new InetSocketAddress(this.adresaPartner, mreznaVrata), 2000);

      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
      out.write(komanda + "\n");
      out.flush();
      mreznaUticnica.shutdownOutput();
      var linija = in.readLine();

      if (komanda.contains("JELOVNIK") || komanda.contains("KARTAPIĆA")
          || komanda.contains("STANJE")) {
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
