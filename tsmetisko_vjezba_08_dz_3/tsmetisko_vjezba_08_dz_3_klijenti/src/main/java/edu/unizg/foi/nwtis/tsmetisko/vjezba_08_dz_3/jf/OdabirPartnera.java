package edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.podaci.Partner;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.ServisPartnerKlijent;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jpa.entiteti.Partneri;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jpa.pomocnici.PartneriFacade;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@RequestScoped
@Named("odabirParnera")
public class OdabirPartnera implements Serializable {

  private static final long serialVersionUID = -524581462819739622L;

  @Inject
  PrijavaKorisnika prijavaKorisnika;

  @Inject
  RestConfiguration restConfiguration;

  @Inject
  PartneriFacade partneriFacade;

  @Inject
  @RestClient
  ServisPartnerKlijent servisPartner;

  private List<Partner> partneri = new ArrayList<>();

  private int partner;

  public int getPartner() {
    return partner;
  }

  public void setPartner(int partner) {
    this.partner = partner;
  }

  public List<Partner> getPartneri() {
    return partneri;
  }

  @PostConstruct
  public void ucitajPartnere() {
    List<Partneri> listaPartneri = this.partneriFacade.findAll();
    this.partneri = this.partneriFacade.pretvori(listaPartneri);
  }

  public String odaberiPartnera() {
    if (this.partner > 0) {
      Optional<Partner> partnerO =
          this.partneri.stream().filter((p) -> p.id() == this.partner).findFirst();
      if (partnerO.isPresent()) {
        this.prijavaKorisnika.setOdabraniPartner(partnerO.get());
        this.prijavaKorisnika.setPartnerOdabran(true);
      } else {
        this.prijavaKorisnika.setPartnerOdabran(false);
      }
    } else {
      this.prijavaKorisnika.setPartnerOdabran(false);
    }
    return "index.html?faces-redirect=true";
  }

  public Partner getPartner(int id) {
    Partneri partner = this.partneriFacade.find(id);
    Partner p = this.partneriFacade.pretvori(partner);
    return p;
  }

  public boolean getOmoguciOdabirPartnera() {
    try {
      var status = servisPartner.getNarudzba(prijavaKorisnika.getKorisnickoIme(),
          prijavaKorisnika.getLozinka());
      if (status.getStatus() == 500 || status.readEntity(String.class).equals("[]")) {
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      return true;
    }
  }

}
