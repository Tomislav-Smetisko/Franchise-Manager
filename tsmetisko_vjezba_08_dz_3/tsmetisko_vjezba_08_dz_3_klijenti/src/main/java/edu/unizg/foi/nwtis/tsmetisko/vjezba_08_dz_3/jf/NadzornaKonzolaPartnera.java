package edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jf;

import java.io.Serializable;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.ServisPartnerKlijent;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@RequestScoped
@Named("nadzornaKonzolaPartnera")
public class NadzornaKonzolaPartnera implements Serializable {

  private static final long serialVersionUID = -15550961362043375L;

  @Inject
  @RestClient
  ServisPartnerKlijent servisPartner;

  private int statusPosluziteljaZaKupce;

  public int getStatusPosluziteljaZaKupce() {
    return statusPosluziteljaZaKupce;
  }

  public void setStatusPosluziteljaZaKupce(int statusPosluziteljaZaKupce) {
    this.statusPosluziteljaZaKupce = statusPosluziteljaZaKupce;
  }

  @PostConstruct
  public void dohvatiStatuse() {
    var statusKupci = this.servisPartner.headPosluziteljStatus(1).getStatus();
    this.statusPosluziteljaZaKupce = statusKupci;
  }

  public String aktivirajPauzu() {
    this.servisPartner.headPauza(1);
    return "nadzornaKonzolaPartnera.xhtml?faces-redirect=true";
  }

  public String aktivirajStart() {
    this.servisPartner.headStart(1);
    return "nadzornaKonzolaPartnera.xhtml?faces-redirect=true";
  }

  public String aktivirajKraj() {
    this.servisPartner.headKraj();
    return "nadzornaKonzolaPartnera.xhtml?faces-redirect=true";
  }

}
