package edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;

/**
 * Klasa KorisnikKupac.
 */
public class KorisnikKupac {

  /** Konfiguracijski podaci. */
  protected Konfiguracija konfig;

  /**
   * Main metoda.
   *
   * @param args argumenti
   */
  public static void main(String[] args) {
    if (args.length != 2) {
      System.out.println("Neispravan broj argumenata");
      return;
    }

    String nazivDatotekeKupca = args[0];
    String nazivDatotekeKomandi = args[1];

    KorisnikKupac kupac = new KorisnikKupac();

    if (!kupac.ucitajKonfiguraciju(nazivDatotekeKupca)) {
      return;
    }

    kupac.ucitajKomande(nazivDatotekeKomandi);

  }

  /**
   * Ucitaj komande.
   *
   * @param nazivDatotekeKomandi naziv datoteke komandi
   */
  private void ucitajKomande(String nazivDatotekeKomandi) {
    var datoteka = Path.of(nazivDatotekeKomandi);
    if (!Files.exists(datoteka) || !Files.isRegularFile(datoteka) || !Files.isReadable(datoteka)) {
      return;
    }

    try (var br = Files.newBufferedReader(datoteka)) {
      String linija;
      while ((linija = br.readLine()) != null) {
        obradiRed(linija);
      }
    } catch (IOException ex) {
    }
  }

  /**
   * Obradi red.
   *
   * @param linija linija
   */
  private void obradiRed(String linija) {
    String[] dio = linija.split(";");
    var korisnik = dio[0];
    var adresa = dio[1];
    var mreznaVrata = Integer.parseInt(dio[2]);
    var spavanje = Integer.parseInt(dio[3]);
    var komanda = dio[4];

    String[] dioKomande = komanda.split(" ");

    String komandaKorisnik = "";
    if (dioKomande.length > 1) {
      komandaKorisnik = dioKomande[1];
    }

    if (komandaKorisnik != "" && !korisnik.equals(komandaKorisnik)) {
      return;
    }

    try {
      Thread.sleep(spavanje);
    } catch (InterruptedException e) {
    }

    posaljiKomandu(adresa, mreznaVrata, komanda);

  }

  /**
   * Posalji komandu.
   *
   * @param adresa adresa
   * @param mreznaVrata mrezna vrata
   * @param komanda komanda
   */
  private void posaljiKomandu(String adresa, int mreznaVrata, String komanda) {
    try {
      var mreznaUticnica = new Socket(adresa, mreznaVrata);
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));

      if (komanda.toUpperCase().contains("JELOVNIK") || komanda.toUpperCase().contains("KARTAPIĆA")
          || komanda.toUpperCase().contains("NARUDŽBA") || komanda.toUpperCase().contains("JELO")
          || komanda.toUpperCase().contains("PIĆE") || komanda.toUpperCase().contains("RAČUN")) {
        out.write(komanda);
      } else {
        mreznaUticnica.shutdownOutput();
        mreznaUticnica.shutdownInput();
        mreznaUticnica.close();
        return;
      }

      out.flush();
      mreznaUticnica.shutdownOutput();

      String red = "";
      StringBuilder odgovor = new StringBuilder();
      while ((red = in.readLine()) != null) {
        odgovor.append(red).append("\n");
        if (red.isBlank() || red.trim().endsWith("]"))
          break;
      }

      mreznaUticnica.shutdownInput();

      mreznaUticnica.close();
    } catch (IOException e) {
    }
  }

  /**
   * Ucitaj konfiguraciju.
   *
   * @param nazivDatoteke naziv datoteke
   * @return true, ako je uspješno učitavanje konfiguracije
   */
  public boolean ucitajKonfiguraciju(String nazivDatoteke) {
    try {
      this.konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
      return true;
    } catch (NeispravnaKonfiguracija ex) {
      Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

}
