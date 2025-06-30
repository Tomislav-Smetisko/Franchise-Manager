package edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.List;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.podaci.Partner;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jpa.entiteti.Zapisi;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jpa.pomocnici.KorisniciFacade;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jpa.pomocnici.ZapisiFacade;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.SecurityContext;

@SessionScoped
@Named("prijavaKorisnika")
public class PrijavaKorisnika implements Serializable {
  private static final long serialVersionUID = -1826447622277477398L;
  private String korisnickoIme;
  private String lozinka;
  private boolean prijavljen = false;
  private String poruka = "";
  private Korisnik korisnik;
  private Partner odabraniPartner;
  private boolean partnerOdabran = false;
  private boolean admin = false;

  @Inject
  RestConfiguration restConfiguration;

  @Inject
  KorisniciFacade korisniciFacade;

  @Inject
  ZapisiFacade zapisiFacade;

  @Inject
  private SecurityContext securityContext;

  public String getKorisnickoIme() {
    return korisnickoIme;
  }

  public void setKorisnickoIme(String korisnickoIme) {
    this.korisnickoIme = korisnickoIme;
  }

  public String getLozinka() {
    return lozinka;
  }

  public void setLozinka(String lozinka) {
    this.lozinka = lozinka;
  }

  public String getIme() {
    return this.korisnik.ime();
  }

  public String getPrezime() {
    return this.korisnik.prezime();
  }

  public String getEmail() {
    return this.korisnik.email();
  }

  public boolean isPrijavljen() {
    if (!this.prijavljen) {
      provjeriPrijavuKorisnika();
    }
    return this.prijavljen;
  }

  public String getPoruka() {
    return poruka;
  }

  public Partner getOdabraniPartner() {
    return odabraniPartner;
  }

  public void setOdabraniPartner(Partner partner) {
    this.odabraniPartner = partner;
  }

  public boolean isPartnerOdabran() {
    return partnerOdabran;
  }

  public void setPartnerOdabran(boolean partnerOdabran) {
    this.partnerOdabran = partnerOdabran;
  }

  public boolean isAdmin() {
    return admin;
  }

  public void setAdmin(boolean admin) {
    this.admin = admin;
  }

  @PostConstruct
  private void provjeriPrijavuKorisnika() {
    if (this.securityContext.getCallerPrincipal() != null) {
      var korIme = this.securityContext.getCallerPrincipal().getName();
      this.korisnik = this.korisniciFacade.pretvori(this.korisniciFacade.find(korIme));
      if (this.korisnik != null) {
        this.prijavljen = true;
        this.korisnickoIme = korIme;
        this.lozinka = this.korisnik.lozinka();

        List<String> uloge = this.korisniciFacade.dohvatiUlogu(korisnickoIme);
        if (!uloge.isEmpty()) {
          for (var u : uloge) {
            if (u.equals("admin")) {
              this.admin = true;
            }
          }
        }
      }
    }
  }

  public String odjavaKorisnika() {
    if (this.prijavljen) {
      this.prijavljen = false;

      FacesContext facesContext = FacesContext.getCurrentInstance();
      facesContext.getExternalContext().invalidateSession();

      InetAddress inet = null;
      String adresaRacunala = null;
      String ipAdresa = null;
      try {
        inet = InetAddress.getLocalHost();
        adresaRacunala = inet.getHostName();
        ipAdresa = inet.getHostAddress();
      } catch (UnknownHostException e) {
      }

      if (inet != null && adresaRacunala != null && ipAdresa != null) {
        Zapisi zapis = new Zapisi();
        zapis.setAdresaracunala(adresaRacunala);
        zapis.setIpadresaracunala(ipAdresa);
        zapis.setKorisnickoime(korisnickoIme);
        zapis.setOpisrada("Odjava");
        zapis.setVrijeme(new Timestamp(System.currentTimeMillis()));

        zapisiFacade.create(zapis);
      }

      return "/index.xhtml?faces-redirect=true";
    }
    return "";
  }

  public String preusmjeri() {
    return "/index.xhtml?faces-redirect=true";
  }

}
