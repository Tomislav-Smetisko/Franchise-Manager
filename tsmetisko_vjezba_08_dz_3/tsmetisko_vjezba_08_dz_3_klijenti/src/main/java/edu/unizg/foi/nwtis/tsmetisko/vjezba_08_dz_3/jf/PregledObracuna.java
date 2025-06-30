package edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.ServisPartnerKlijent;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jpa.entiteti.Obracuni;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jpa.pomocnici.ObracuniFacade;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@RequestScoped
@Named("pregledObracuna")
public class PregledObracuna implements Serializable {

  private static final long serialVersionUID = 4997096566189648377L;

  @Inject
  @RestClient
  ServisPartnerKlijent servisPartner;

  @Inject
  ObracuniFacade obracuniFacade;

  private int partner;

  private String vrijemeOd;

  private String vrijemeDo;

  private List<Obracuni> rezultati;

  public int getPartner() {
    return partner;
  }

  public void setPartner(int partner) {
    this.partner = partner;
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

  public List<Obracuni> getRezultati() {
    return rezultati;
  }

  public void setRezultati(List<Obracuni> rezultati) {
    this.rezultati = rezultati;
  }

  public void dohvatiObracune() {
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

    List<Obracuni> obracuni =
        this.obracuniFacade.dohvatiObracuneZaPartnera(this.partner, vrijemeOdLong, vrijemeDoLong);
    this.rezultati = obracuni;
  }

}
