/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.podaci.Obracun;
import edu.unizg.foi.nwtis.podaci.Partner;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jf.PrijavaKorisnika;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.mvc.Controller;
import jakarta.mvc.Models;
import jakarta.mvc.View;
import jakarta.mvc.binding.BindingResult;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.GenericType;

/**
 *
 * @author NWTiS
 */
@Controller
@Path("tvrtka")
@RequestScoped
public class Kontroler {

  @Inject
  private Models model;

  @Inject
  private BindingResult bindingResult;

  @Inject
  @RestClient
  ServisTvrtkaKlijent servisTvrtka;

  @Inject
  PrijavaKorisnika prijavaKorisnika;

  @GET
  @Path("pocetak")
  @View("index.jsp")
  public void pocetak() {
    this.model.put("prijavljen", this.prijavaKorisnika.isPrijavljen());
    this.model.put("admin", this.prijavaKorisnika.isAdmin());
  }

  @GET
  @Path("privatno/pocetakTvrtkaPrijavljen")
  @View("pocetakTvrtkaPrijavljen.jsp")
  public void pocetakTvrtkaPrijavljen() {
    this.model.put("prijavljen", this.prijavaKorisnika.isPrijavljen());
    this.model.put("admin", this.prijavaKorisnika.isAdmin());
  }

  @GET
  @Path("kraj")
  @View("status.jsp")
  public void kraj() {
    var status = this.servisTvrtka.headPosluziteljKraj().getStatus();
    this.model.put("statusOperacije", status);
    dohvatiStatuse();
  }

  @GET
  @Path("status")
  @View("status.jsp")
  public void status() {
    dohvatiStatuse();
  }

  @GET
  @Path("start/{id}")
  @View("status.jsp")
  public void startId(@PathParam("id") int id) {
    var status = this.servisTvrtka.headPosluziteljStart(id).getStatus();
    this.model.put("status", status);
    this.model.put("samoOperacija", true);
  }

  @GET
  @Path("pauza/{id}")
  @View("status.jsp")
  public void pauzatId(@PathParam("id") int id) {
    var status = this.servisTvrtka.headPosluziteljPauza(id).getStatus();
    this.model.put("status", status);
    this.model.put("samoOperacija", true);
  }

  @GET
  @Path("partner")
  @View("partneri.jsp")
  public void partneri() {
    var odgovor = this.servisTvrtka.getPartneri();
    var status = odgovor.getStatus();
    if (status == 200) {
      var partneri = odgovor.readEntity(new GenericType<List<Partner>>() {});
      this.model.put("status", status);
      this.model.put("partneri", partneri);
    }
  }

  @GET
  @Path("partner/{id}")
  @View("pregledOdabranogPartnera.jsp")
  public void pregledPartnera(@PathParam("id") int id) {
    var odgovor = this.servisTvrtka.getPartner(id);
    var status = odgovor.getStatus();
    if (status == 200) {
      var partner = odgovor.readEntity(new GenericType<Partner>() {});
      this.model.put("status", status);
      this.model.put("partner", partner);
    }
  }

  private void dohvatiStatuse() {
    try {
      this.model.put("samoOperacija", false);
      var statusT = this.servisTvrtka.headPosluzitelj().getStatus();
      this.model.put("statusT", statusT);
      var statusT1 = this.servisTvrtka.headPosluziteljStatus(1).getStatus();
      this.model.put("statusT1", statusT1);
      var statusT2 = this.servisTvrtka.headPosluziteljStatus(2).getStatus();
      this.model.put("statusT2", statusT2);

    } catch (Exception e) {
      this.model.put("statusT", 204);
      this.model.put("statusT1", 204);
      this.model.put("statusT2", 204);
    }


  }

  @GET
  @Path("admin/nadzornaKonzolaTvrtka")
  @View("nadzornaKonzolaTvrtka.jsp")
  public void nadzornaKonzolaTvrtka() {
    dohvatiStatuse();

  }

  @GET
  @Path("admin/pauzaKonzola/{id}")
  @View("nadzornaKonzolaTvrtka.jsp")
  public void pauzaUKonzoli(@PathParam("id") int id) {
    var status = this.servisTvrtka.headPosluziteljPauza(id).getStatus();
    this.model.put("status", status);
    this.model.put("samoOperacija", true);
    dohvatiStatuse();
  }

  @GET
  @Path("admin/startKonzola/{id}")
  @View("nadzornaKonzolaTvrtka.jsp")
  public void startKonzola(@PathParam("id") int id) {
    var status = this.servisTvrtka.headPosluziteljStart(id).getStatus();
    this.model.put("status", status);
    this.model.put("samoOperacija", true);
    dohvatiStatuse();
  }

  @GET
  @Path("admin/krajKonzola")
  @View("nadzornaKonzolaTvrtka.jsp")
  public void krajKonzola() {
    var status = this.servisTvrtka.headPosluziteljKraj().getStatus();
    this.model.put("statusOperacije", status);
    dohvatiStatuse();
  }

  @GET
  @Path("privatno/obracuni")
  @View("obracuni.jsp")
  public void obracun(@QueryParam("od") String vrijemeOd, @QueryParam("do") String vrijemeDo,
      @QueryParam("vrsta") String vrsta) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    Long vrijemeOdLong = null;
    Long vrijemeDoLong = null;
    if (vrijemeOd != null && !vrijemeOd.isBlank()) {
      vrijemeOdLong = LocalDateTime.parse(vrijemeOd, formatter).atZone(ZoneId.systemDefault())
          .toInstant().toEpochMilli();
    }
    if (vrijemeDo != null && !vrijemeDo.isBlank()) {
      vrijemeDoLong = LocalDateTime.parse(vrijemeDo, formatter).atZone(ZoneId.systemDefault())
          .toInstant().toEpochMilli();
    }

    vrsta = vrsta == null ? "jeloPice" : vrsta;
    dohvatiObracune(vrsta, vrijemeOdLong, vrijemeDoLong);
  }

  private void dohvatiObracune(String vrsta, Long vrijemeOdLong, Long vrijemeDoLong) {
    switch (vrsta) {
      case "jeloPice":
        var odgovor = this.servisTvrtka.getObracun(vrijemeOdLong, vrijemeDoLong);
        var status = odgovor.getStatus();
        if (status == 200) {
          var obracuni = odgovor.readEntity(new GenericType<List<Obracun>>() {});
          this.model.put("status", status);
          this.model.put("obracuni", obracuni);
        }
        break;

      case "jelo":
        var odgovorJelo = this.servisTvrtka.getObracunJelo(vrijemeOdLong, vrijemeDoLong);
        var statusJelo = odgovorJelo.getStatus();
        if (statusJelo == 200) {
          var obracuni = odgovorJelo.readEntity(new GenericType<List<Obracun>>() {});
          this.model.put("status", statusJelo);
          this.model.put("obracuni", obracuni);
        }
        break;

      case "pice":
        var odgovorPice = this.servisTvrtka.getObracunPice(vrijemeOdLong, vrijemeDoLong);
        var statusPice = odgovorPice.getStatus();
        if (statusPice == 200) {
          var obracuni = odgovorPice.readEntity(new GenericType<List<Obracun>>() {});
          this.model.put("status", statusPice);
          this.model.put("obracuni", obracuni);
        }
        break;
    }
  }

  @GET
  @Path("privatno/obracuni/partner")
  @View("obracuni.jsp")
  public void obracunPartner(@QueryParam("id") int id, @QueryParam("od") String vrijemeOd,
      @QueryParam("do") String vrijemeDo, @QueryParam("vrsta") String vrsta) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    Long vrijemeOdLong = null;
    Long vrijemeDoLong = null;
    if (vrijemeOd != null && !vrijemeOd.isBlank()) {
      vrijemeOdLong = LocalDateTime.parse(vrijemeOd, formatter).atZone(ZoneId.systemDefault())
          .toInstant().toEpochMilli();
    }
    if (vrijemeDo != null && !vrijemeDo.isBlank()) {
      vrijemeDoLong = LocalDateTime.parse(vrijemeDo, formatter).atZone(ZoneId.systemDefault())
          .toInstant().toEpochMilli();
    }

    var odgovor = this.servisTvrtka.getObracunPartner(id, vrijemeOdLong, vrijemeDoLong);
    var status = odgovor.getStatus();
    if (status == 200) {
      var obracuni = odgovor.readEntity(new GenericType<List<Obracun>>() {});
      this.model.put("status", status);
      this.model.put("obracuni", obracuni);
    }
  }

  @GET
  @Path("admin/prikazDodavanjaPartnera")
  @View("noviPartner.jsp")
  public void prikazDodavanjaPartnera() {}

  @POST
  @Path("admin/dodajPartnera")
  @View("noviPartner.jsp")
  public void dodajPartnera(@FormParam("id") int id, @FormParam("naziv") String naziv,
      @FormParam("vk") String vk, @FormParam("adresa") String adresa, @FormParam("mv") int mv,
      @FormParam("mvk") int mvk, @FormParam("gpsSirina") float gpsSirina,
      @FormParam("gpsDuzina") float gpsDuzina, @FormParam("sigurnosniKod") String sigurnosniKod,
      @FormParam("adminKod") String adminKod) {

    Partner partner =
        new Partner(id, naziv, vk, adresa, mv, gpsSirina, gpsDuzina, mvk, sigurnosniKod, adminKod);

    var odgovor = this.servisTvrtka.postPartner(partner);
  }

  @GET
  @Path("admin/spava")
  @View("spavanje.jsp")
  public void otvoriSpava() {}

  @GET
  @Path("admin/spavanje")
  @View("spavanje.jsp")
  public void spava(@QueryParam("vrijeme") long vrijeme) {
    var odgovor = this.servisTvrtka.getSpava(vrijeme);
  }

}
