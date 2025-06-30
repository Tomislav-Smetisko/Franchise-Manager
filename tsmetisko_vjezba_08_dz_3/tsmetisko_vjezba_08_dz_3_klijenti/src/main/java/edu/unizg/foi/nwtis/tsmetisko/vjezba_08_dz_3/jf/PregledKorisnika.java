package edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import com.google.gson.Gson;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.ServisPartnerKlijent;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jpa.entiteti.Korisnici;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jpa.pomocnici.KorisniciFacade;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@RequestScoped
@Named("pregledKorisnika")
public class PregledKorisnika implements Serializable {

  private static final long serialVersionUID = 2741631963622921532L;

  @Inject
  @RestClient
  ServisPartnerKlijent servisPartner;

  @Inject
  KorisniciFacade korisniciFacade;

  private List<Korisnik> korisnici = new ArrayList<>();

  private String ime;

  private String prezime;

  private String id;

  public List<Korisnik> getKorisnici() {
    return korisnici;
  }

  public void setKorisnici(List<Korisnik> korisnici) {
    this.korisnici = korisnici;
  }

  public String getIme() {
    return ime;
  }

  public void setIme(String ime) {
    this.ime = ime;
  }

  public String getPrezime() {
    return prezime;
  }

  public void setPrezime(String prezime) {
    this.prezime = prezime;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @PostConstruct
  public void ucitajKorisnike() {
    List<Korisnici> kE = korisniciFacade.findAll();
    korisnici = korisniciFacade.pretvori(kE);
  }

  public void pretraziKorisnika() {
    List<Korisnici> kE = korisniciFacade.findAll(prezime, ime);
    korisnici = korisniciFacade.pretvori(kE);
    System.out.println("Korisnici: " + korisnici);
  }

  public List<Korisnik> dohvatiImePrezimeKorisnika() {
    String odgovor = this.servisPartner.getKorisnik().readEntity(String.class);
    Gson gson = new Gson();
    Korisnik[] niz = gson.fromJson(odgovor, Korisnik[].class);
    List<Korisnik> lista = Arrays.asList(niz);
    return lista;
  }

  public Korisnik getKorisnik(String id) {
    return this.servisPartner.getKorisnikId(id).readEntity(Korisnik.class);
  }

}
