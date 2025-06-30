package edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jf;

import java.io.Serializable;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.ServisPartnerKlijent;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jpa.entiteti.Korisnici;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jpa.entiteti.Uloge;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jpa.pomocnici.KorisniciFacade;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jpa.pomocnici.UlogeFacade;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("korisnici")
@RequestScoped
public class KorisnikRest implements Serializable {

  private static final long serialVersionUID = 7770823023747143122L;

  @Inject
  @RestClient
  ServisPartnerKlijent servisPartner;

  @Inject
  KorisniciFacade korisniciFacade;

  @Inject
  UlogeFacade ulogeFacade;

  private Korisnici korisnik = new Korisnici();

  public Korisnici getKorisnik() {
    return korisnik;
  }

  public void setKorisnik(Korisnici korisnik) {
    this.korisnik = korisnik;
  }

  public void postKorisnik() {
    try {
      Korisnik k = this.korisniciFacade.pretvori(this.korisnik);
      this.servisPartner.postKorisnik(k);

      Uloge uloga = new Uloge();
      uloga.setKorisnik(k.korisnik());
      uloga.setGrupa("nwtis");
      this.ulogeFacade.create(uloga);
    } catch (Exception e) {
    }
  }

}
