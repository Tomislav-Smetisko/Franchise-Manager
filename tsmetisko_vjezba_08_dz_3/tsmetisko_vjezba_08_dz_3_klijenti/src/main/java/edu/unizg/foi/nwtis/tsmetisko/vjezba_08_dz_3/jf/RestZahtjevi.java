package edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import com.google.gson.Gson;
import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.GlobalniPodaci;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.ServisPartnerKlijent;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jpa.entiteti.Zapisi;
import edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3.jpa.pomocnici.ZapisiFacade;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@RequestScoped
@Named("restZahtjevi")
public class RestZahtjevi implements Serializable {

  private static final long serialVersionUID = 3204300222607791208L;
  @Inject
  @RestClient
  ServisPartnerKlijent servisPartner;

  @Inject
  PrijavaKorisnika prijavaKorisnika;

  @Inject
  ZapisiFacade zapisiFacade;

  @Inject
  GlobalniPodaci globalniPodaci;

  public boolean getStatus() {
    try {
      return servisPartner.headPosluzitelj().getStatus() == 200;
    } catch (Exception e) {
      return false;
    }
  }

  public List<Jelovnik> getJelovnik() {
    String odgovor = servisPartner
        .getJelovnik(prijavaKorisnika.getKorisnickoIme(), prijavaKorisnika.getLozinka())
        .readEntity(String.class);
    Gson gson = new Gson();
    Jelovnik[] niz = gson.fromJson(odgovor, Jelovnik[].class);
    List<Jelovnik> lista = Arrays.asList(niz);

    return lista;
  }

  public List<KartaPica> getKartaPica() {
    String odgovor = servisPartner
        .getKartaPica(prijavaKorisnika.getKorisnickoIme(), prijavaKorisnika.getLozinka())
        .readEntity(String.class);
    Gson gson = new Gson();
    KartaPica[] niz = gson.fromJson(odgovor, KartaPica[].class);
    List<KartaPica> lista = Arrays.asList(niz);

    return lista;
  }

  public String postNarudzba() {
    try {
      var status = servisPartner
          .postNarudzba(prijavaKorisnika.getKorisnickoIme(), prijavaKorisnika.getLozinka())
          .getStatus();
      if (status == 201) {
        this.globalniPodaci
            .povecajBrojOtvorenihNarudzbi(this.prijavaKorisnika.getOdabraniPartner().id());
        dodajZapis("Kreirana nova narudžba");
      } else {
        return "index.xhtml?faces-redirect=true";
      }
    } catch (Exception e) {
      return "index.xhtml?faces-redirect=true";
    }
    return "privatno/novaNarudzba.xhtml?faces-redirect=true";
  }

  public String dopunaNarudzbe() {
    try {
      var status = servisPartner
          .getNarudzba(prijavaKorisnika.getKorisnickoIme(), prijavaKorisnika.getLozinka())
          .getStatus();
      if (status == 200) {
        return "privatno/dopunaNarudzbe.xhtml?faces-redirect=true";
      } else {
        return "index.xhtml?faces-redirect=true";
      }
    } catch (Exception e) {
      return "index.xhtml?faces-redirect=true";
    }
  }

  public String placanjeNarudzbe() {
    try {
      var status = servisPartner
          .getNarudzba(prijavaKorisnika.getKorisnickoIme(), prijavaKorisnika.getLozinka())
          .getStatus();
      if (status == 200) {
        return "privatno/placanjeNarudzbe.xhtml?faces-redirect=true";
      } else {
        return "index.xhtml?faces-redirect=true";
      }
    } catch (Exception e) {
      return "index.xhtml?faces-redirect=true";
    }
  }

  public String postRacun() {
    try {
      var status = servisPartner
          .postRacun(prijavaKorisnika.getKorisnickoIme(), prijavaKorisnika.getLozinka())
          .getStatus();
      if (status == 201) {
        this.globalniPodaci
            .smanjiBrojOtvorenihNarudzbi(this.prijavaKorisnika.getOdabraniPartner().id());
        this.globalniPodaci.povecajBrojRacuna(this.prijavaKorisnika.getOdabraniPartner().id());
        dodajZapis("Plaćen račun");
      }
      return "/index.xhtml?faces-redirect=true";
    } catch (Exception e) {
    }
    return "";
  }

  public void postJelo() {
    try {
      // var status = servisPartner
      // .postJelo(prijavaKorisnika.getKorisnickoIme(), prijavaKorisnika.getLozinka()).getStatus();
    } catch (Exception e) {
    }
  }

  public void dodajZapis(String poruka) {
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
      zapis.setKorisnickoime(prijavaKorisnika.getKorisnickoIme());
      zapis.setOpisrada(poruka);
      zapis.setVrijeme(new Timestamp(System.currentTimeMillis()));

      zapisiFacade.create(zapis);
    }
  }

}
