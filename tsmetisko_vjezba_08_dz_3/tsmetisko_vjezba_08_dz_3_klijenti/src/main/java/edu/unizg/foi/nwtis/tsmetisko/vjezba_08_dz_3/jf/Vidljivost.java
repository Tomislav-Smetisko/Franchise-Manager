package edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jf;

import java.io.Serializable;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.ServisPartnerKlijent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@RequestScoped
@Named("vidljivost")
public class Vidljivost implements Serializable {

  private static final long serialVersionUID = 8841224768170294838L;

  @Inject
  @RestClient
  ServisPartnerKlijent servisPartner;

  @Inject
  PrijavaKorisnika prijavaKorisnika;

  public boolean novaNarudzba() {
    try {
      var narudzba = this.servisPartner.getNarudzba(this.prijavaKorisnika.getKorisnickoIme(),
          this.prijavaKorisnika.getLozinka());

      return false;
    } catch (Exception e) {
      return true;
    }
  }

  public boolean dopunaPlacanje() {
    try {
      var narudzba = this.servisPartner.getNarudzba(this.prijavaKorisnika.getKorisnickoIme(),
          this.prijavaKorisnika.getLozinka());
      if (narudzba.getStatus() == 200) {
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      return false;
    }
  }

}
