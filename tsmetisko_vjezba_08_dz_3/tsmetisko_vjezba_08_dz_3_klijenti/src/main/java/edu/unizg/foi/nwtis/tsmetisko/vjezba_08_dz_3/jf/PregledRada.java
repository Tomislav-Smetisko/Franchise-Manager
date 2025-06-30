package edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jpa.entiteti.Korisnici;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jpa.entiteti.Zapisi;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jpa.pomocnici.KorisniciFacade;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jpa.pomocnici.ZapisiFacade;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@RequestScoped
@Named("pregledRada")
public class PregledRada implements Serializable {

  private static final long serialVersionUID = 753152839276869746L;

  @Inject
  KorisniciFacade korisniciFacade;

  @Inject
  ZapisiFacade zapisiFacade;

  private String korisnik;

  private String vrijemeOd;

  private String vrijemeDo;

  private List<Zapisi> rezultati;

  public String getKorisnik() {
    return korisnik;
  }

  public void setKorisnik(String korisnik) {
    this.korisnik = korisnik;
  }

  public String getVrijemeOd() {
    return vrijemeOd;
  }

  public void setVrijemeOd(String vrijemeOd) {
    this.vrijemeOd = vrijemeOd;
  }

  public String getVrijemeDo() {
    return vrijemeDo;
  }

  public void setVrijemeDo(String vrijemeDo) {
    this.vrijemeDo = vrijemeDo;
  }

  public List<Zapisi> getRezultati() {
    return rezultati;
  }

  public void setRezultati(List<Zapisi> rezultati) {
    this.rezultati = rezultati;
  }

  public List<Korisnik> dohvatiKorisnike() {
    List<Korisnici> korisnici = this.korisniciFacade.findAll();
    return this.korisniciFacade.pretvori(korisnici);
  }

  public void dohvatiZapise() {
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

    List<Zapisi> zapisi =
        this.zapisiFacade.dohvatiZapiseZaPartnera(this.korisnik, vrijemeOdLong, vrijemeDoLong);
    this.rezultati = zapisi;
  }

}
