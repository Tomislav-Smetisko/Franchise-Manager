package edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;
import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import edu.unizg.foi.nwtis.podaci.Narudzba;
import edu.unizg.foi.nwtis.podaci.Obracun;

/**
 * Klasa PosluziteljPartner.
 */
public class PosluziteljPartner {

  /** Konfiguracijski podaci. */
  private Konfiguracija konfig;

  /** Pokretač dretvi. */
  private ExecutorService executor = null;

  /** Jelovnik. */
  private Map<String, Jelovnik> jelovnik = new ConcurrentHashMap<>();

  /** Karta pića. */
  private Map<String, KartaPica> kartaPica = new ConcurrentHashMap<>();

  /** Otvorene narudžbe. */
  private Map<String, Queue<Narudzba>> narudzbeOtvorene = new ConcurrentHashMap<>();

  /** Plaćene narudžbe. */
  private Queue<Narudzba> placeneNarudzbe = new ConcurrentLinkedQueue<>();

  /** Broj naplaćenih narudžbi. */
  private int brojNaplacenihNarudzbi = 0;

  /** Predložak za kraj. */
  private Pattern predlozakKraj = Pattern.compile("^KRAJ$");

  /** The predložak registracija. */
  private Pattern predlozakRegistracija = Pattern.compile(
      "^(?<komanda>PARTNER) (?<id>\\d+) (?<partner>(.*?)) (?<kuhinja>\\w+) (?<adresa>(localhost|[\\d\\.]+)) (?<vrata>\\d+) (?<sirina>\\d+(\\.\\d+)?) (?<duzina>\\d+(\\.\\d+)?)$");

  /** Predložak briši. */
  private Pattern predlozakBrisi = Pattern.compile("^OBRIŠI \\d+ [\\d\\w]+$");

  /** Predložak jelovnik. */
  private Pattern predlozakJelovnik = Pattern.compile("^JELOVNIK (\\w)+$");

  /** Predložak karta pića. */
  private Pattern predlozakKartaPica = Pattern.compile("^KARTAPIĆA (\\w)+$");

  /** Predložak narudžba. */
  private Pattern predlozakNarudzba = Pattern.compile("^NARUDŽBA (\\w+)$");

  /** Predložak jelo. */
  private Pattern predlozakJelo = Pattern.compile("^(JELO) (\\w+) ([\\w\\d]+) (\\d+(\\.\\d+)?)$");

  /** Predložak pice. */
  private Pattern predlozakPice = Pattern.compile("^(PIĆE) (\\w+) ([\\w\\d]+) (\\d+(\\.\\d+)?)$");

  /** Predložak račun. */
  private Pattern predlozakRacun = Pattern.compile("^(RAČUN) (\\w+)$");

  /** Predložak status. */
  private Pattern predlozakStatus = Pattern.compile("^(STATUS) ([\\w\\d]+) (1)$");

  /** Predložak pauza. */
  private Pattern predlozakPauza = Pattern.compile("^(PAUZA) ([\\w\\d]+) (1)$");

  /** Predložak start. */
  private Pattern predlozakStart = Pattern.compile("^(START) ([\\w\\d]+) (1)$");

  /** Predložak spava. */
  private Pattern predlozakSpava = Pattern.compile("^(SPAVA) ([\\w\\d]+) (\\d+)$");

  /** Predložak osvježi. */
  private Pattern predlozakOsvjezi = Pattern.compile("^(OSVJEŽI) ([\\w\\d]+)$");

  /** Predložak stanje. */
  private Pattern predlozakStanje = Pattern.compile("^(STANJE) (\\w+)$");

  /** Jedna narudžba. */
  private final Object jednaNarudzba = new Object();

  /** Jedno jelo. */
  private final Object jednoJelo = new Object();

  /** Jedno piće. */
  private final Object jednoPice = new Object();

  /** Jedan račun. */
  private final Object jedanRacun = new Object();

  /** Zastavica za kraj rada. */
  private AtomicBoolean kraj = new AtomicBoolean(false);

  /** Kod za kraj rada. */
  private String kodZaKraj = "";

  /** Status poslužitelja za kupce. */
  private AtomicInteger statusPosluziteljaZaKupce = new AtomicInteger(1);

  /**
   * Main metoda.
   *
   * @param args argumenti
   */
  public static void main(String[] args) {

    var program = new PosluziteljPartner();
    var nazivDatoteke = args[0];

    if (!program.ucitajKonfiguraciju(nazivDatoteke)) {
      return;
    }

    if (args.length == 1) {
      program.posaljiRegistraciju();
      // return;
    }

    String linija = "";
    if (args.length > 1) {
      linija = args[1];
    }

    var poklapanje = program.predlozakKraj.matcher(linija);
    var status = poklapanje.matches();

    if (status) {
      program.posaljiKraj();
      return;
    } else if (linija.equals("PARTNER")) {
      program.pripremiKreni(nazivDatoteke);
    }
  }

  /**
   * Pripremi kreni.
   *
   * @param nazivDatoteke naziv datoteke
   */
  public void pripremiKreni(String nazivDatoteke) {
    var adresa = this.konfig.dajPostavku("adresa").trim();
    var mreznaVrataRad = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRad").trim());
    var brojCekaca = Integer.parseInt(this.konfig.dajPostavku("brojCekaca").trim());
    var id = this.konfig.dajPostavku("id").trim();
    var sigurnosniKod = this.konfig.dajPostavku("sigKod");
    this.kodZaKraj = this.konfig.dajPostavku("kodZaKraj");
    var pauzaDretve = Integer.parseInt(this.konfig.dajPostavku("pauzaDretve").trim());

    if (sigurnosniKod == null) {
      return;
    } else {
      sigurnosniKod = sigurnosniKod.trim();
    }

    var builder = Thread.ofVirtual();
    var factory = builder.factory();
    this.executor = Executors.newThreadPerTaskExecutor(factory);

    String rezultatJelovnik = posaljiJelovnik(adresa, mreznaVrataRad, id, sigurnosniKod);
    String rezultatKartaPica = posaljiKartuPica(adresa, mreznaVrataRad, id, sigurnosniKod);
    if (!rezultatJelovnik.contains("OK")) {
      System.out.println("ERROR 46 - Neuspješno preuzimanje jelovnika\n");
      return;
    } else if (!rezultatKartaPica.contains("OK")) {
      System.out.println("ERROR 47 - Neuspješno preuzimanje karte pića");
      return;
    }

    var dretvaZaKraj = this.executor.submit(() -> this.pokreniPosluziteljKraj());
    var dretva = this.executor.submit(() -> this.pokreniPosluzitelj());

    while (!dretvaZaKraj.isDone()) {
      try {
        Thread.sleep(pauzaDretve);
      } catch (InterruptedException e) {
      }
    }

    // pokreniPosluzitelj();
  }

  /**
   * Pokreni posluzitelj.
   */
  public void pokreniPosluzitelj() {
    var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrata").trim());
    var brojCekaca = Integer.parseInt(this.konfig.dajPostavku("brojCekaca").trim());
    var pauzaDretve = Integer.parseInt(this.konfig.dajPostavku("pauzaDretve").trim());
    try (ServerSocket ss = new ServerSocket(mreznaVrata, brojCekaca)) {
      while (!this.kraj.get()) {
        var mreznaUticnica = ss.accept();
        this.executor.submit(() -> this.obradiRad(mreznaUticnica));
      }
      ss.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Pokreni poslužitelj kraj.
   */
  public void pokreniPosluziteljKraj() {
    var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataKrajPartner"));
    var brojCekaca = Integer.parseInt(this.konfig.dajPostavku("brojCekaca"));
    try (ServerSocket ss = new ServerSocket(mreznaVrata, brojCekaca)) {
      while (!this.kraj.get()) {
        var mreznaUticnica = ss.accept();
        this.obradiKraj(mreznaUticnica);
      }
      ss.close();

    } catch (IOException e) {
    }
  }

  /**
   * Obradi kraj.
   *
   * @param mreznaUticnica mrezna uticnica
   * @return true, ako se program uspješno gasi
   */
  public Boolean obradiKraj(Socket mreznaUticnica) {
    try {
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
      String linija = in.readLine();
      mreznaUticnica.shutdownInput();

      if (linija.trim().equals("KRAJ " + this.kodZaKraj)) {
        out.write("OK\n");
        this.kraj.set(true);
      } else if (linija.toUpperCase().contains("STATUS")) {
        String returnMessage = obradiStatus(linija);
        out.write(returnMessage);
      } else if (linija.toUpperCase().contains("PAUZA")) {
        String returnMessage = obradiPauzu(linija);
        out.write(returnMessage);
      } else if (linija.toUpperCase().contains("START")) {
        String returnMessage = obradiStart(linija);
        out.write(returnMessage);
      } else if (linija.toUpperCase().contains("SPAVA")) {
        String returnMessage = obradiSpava(linija);
        out.write(returnMessage);
      } else if (linija.toUpperCase().contains("OSVJEŽI")) {
        String returnMessage = obradiOsvjezi(linija);
        out.write(returnMessage);
      } else {
        out.write("ERROR 10 - Format komande nije ispravan ili nije ispravan kod za kraj\n");
      }

      out.flush();
      mreznaUticnica.shutdownOutput();
      mreznaUticnica.close();
    } catch (Exception e) {

    }
    return Boolean.TRUE;
  }

  /**
   * Obradi rad.
   *
   * @param mreznaUticnica mrezna uticnica
   */
  public void obradiRad(Socket mreznaUticnica) {
    try {
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));

      if (statusPosluziteljaZaKupce.get() == 0) {
        out.write("ERROR 48 - Poslužitelj za prijem zahtjeva kupaca u pauzi\n");
        out.flush();
        mreznaUticnica.shutdownOutput();
        mreznaUticnica.close();
        return;
      }

      String linija = in.readLine();
      mreznaUticnica.shutdownInput();

      obradiKomandu(out, linija);

      out.flush();
      mreznaUticnica.shutdownOutput();
      mreznaUticnica.close();
    } catch (Exception e) {

    }
  }

  /**
   * Obradi komandu.
   *
   * @param out out
   * @param linija linija
   */
  private void obradiKomandu(PrintWriter out, String linija) {
    if (linija.toUpperCase().contains("JELOVNIK")) {
      String returnMessage = obradiJelovnik(linija);
      out.write(returnMessage);
    } else if (linija.toUpperCase().contains("KARTAPIĆA")) {
      String returnMessage = obradiKartuPica(linija);
      out.write(returnMessage);
    } else if (linija.toUpperCase().contains("NARUDŽBA")) {
      String returnMessage;
      synchronized (jednaNarudzba) {
        returnMessage = obradiNarudzbu(linija);
      }
      out.write(returnMessage);
    } else if (linija.toUpperCase().contains("JELO")) {
      String returnMessage;
      synchronized (jednoJelo) {
        returnMessage = obradiJelo(linija);
      }
      out.write(returnMessage);
    } else if (linija.toUpperCase().contains("PIĆE")) {
      String returnMessage;
      synchronized (jednoPice) {
        returnMessage = obradiPice(linija);
      }
      out.write(returnMessage);
    } else if (linija.toUpperCase().contains("RAČUN")) {
      String returnMessage;
      synchronized (jedanRacun) {
        returnMessage = obradiRacun(linija);
      }
      out.write(returnMessage);
    } else if (linija.toUpperCase().contains("STANJE")) {
      String returnMessage = obradiStanje(linija);
      out.write(returnMessage);
    }
  }

  /**
   * Obradi stanje.
   *
   * @param linija the linija
   * @return the string
   */
  private String obradiStanje(String linija) {
    var poklapanje = predlozakStanje.matcher(linija);
    var status = poklapanje.matches();

    if (!status) {
      return "ERROR 60 - Format komande nije ispravan\n";
    }

    String korisnik = poklapanje.group(2);
    if (!narudzbeOtvorene.containsKey(korisnik)) {
      return "ERROR 43 - Ne postoji otvorena narudžba za korisnika/kupca\n";
    }

    Queue<Narudzba> narudzbe = narudzbeOtvorene.get(korisnik);
    Gson gson = new GsonBuilder().create();
    String json = gson.toJson(narudzbe);

    return "OK\n" + json + "\n";
  }

  /**
   * Obradi osvjezi.
   *
   * @param linija the linija
   * @return the string
   */
  private String obradiOsvjezi(String linija) {
    var poklapanje = predlozakOsvjezi.matcher(linija);
    var status = poklapanje.matches();

    if (!status) {
      return "ERROR 60 - Format komande nije ispravan\n";
    }

    var kodZaAdminPartnera = this.konfig.dajPostavku("kodZaAdmin");
    var kodIzKomande = poklapanje.group(2);
    if (!kodZaAdminPartnera.equals(kodIzKomande)) {
      return "ERROR 61 - Pogrešan kodZaAdminPartnera\n";
    }

    if (statusPosluziteljaZaKupce.get() == 1) {
      var adresa = this.konfig.dajPostavku("adresa");
      var mreznaVrataRad = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRad"));
      var id = this.konfig.dajPostavku("id");
      var sigurnosniKod = this.konfig.dajPostavku("sigKod");

      String odgovorKartaPica = posaljiKartuPica(adresa, mreznaVrataRad, id, sigurnosniKod);
      String odgovorJelovnik = posaljiJelovnik(adresa, mreznaVrataRad, id, sigurnosniKod);

      if (odgovorKartaPica.startsWith("OK") && odgovorJelovnik.startsWith("OK")) {
        return "OK\n";
      } else {
        return "ERROR 49 - Neuspješno preuzimanje karte pića i jelovnika\n";
      }
    }

    return "ERROR 49 - Poslužitelj za kupce je u pauzi\n";
  }

  /**
   * Obradi spava.
   *
   * @param linija the linija
   * @return the string
   */
  private String obradiSpava(String linija) {
    var poklapanje = predlozakSpava.matcher(linija);
    var status = poklapanje.matches();

    if (!status) {
      return "ERROR 60 - Format komande nije ispravan\n";
    }

    var kodZaAdminPartnera = this.konfig.dajPostavku("kodZaAdmin");
    var kodIzKomande = poklapanje.group(2);
    if (!kodZaAdminPartnera.equals(kodIzKomande)) {
      return "ERROR 61 - Pogrešan kodZaAdminPartnera\n";
    }

    var duzinaSpavanja = Long.parseLong(poklapanje.group(3));
    try {
      Thread.sleep(duzinaSpavanja);
    } catch (InterruptedException e) {
      return "ERROR 63 – Prekid spavanja dretve\n";
    }

    return "OK\n";
  }

  /**
   * Obradi start.
   *
   * @param linija the linija
   * @return the string
   */
  private String obradiStart(String linija) {
    var poklapanje = predlozakStart.matcher(linija);
    var status = poklapanje.matches();

    if (!status) {
      return "ERROR 60 - Format komande nije ispravan\n";
    }

    var kodZaAdminPartnera = this.konfig.dajPostavku("kodZaAdmin");
    var kodIzKomande = poklapanje.group(2);
    if (!kodZaAdminPartnera.equals(kodIzKomande)) {
      return "ERROR 61 – Pogrešan kodZaAdminPartnera\n";
    }

    if (statusPosluziteljaZaKupce.get() == 0) {
      statusPosluziteljaZaKupce.set(1);
      return "OK\n";
    }

    return "ERROR 62 – Pogrešna promjena pauze ili starta\n";
  }

  /**
   * Obradi pauzu.
   *
   * @param linija the linija
   * @return the string
   */
  private String obradiPauzu(String linija) {
    var poklapanje = predlozakPauza.matcher(linija);
    var status = poklapanje.matches();

    if (!status) {
      return "ERROR 60 - Format komande nije ispravan\n";
    }

    var kodZaAdminPartnera = this.konfig.dajPostavku("kodZaAdmin");
    var kodIzKomande = poklapanje.group(2);
    if (!kodZaAdminPartnera.equals(kodIzKomande)) {
      return "ERROR 61 – Pogrešan kodZaAdminPartnera\n";
    }

    if (statusPosluziteljaZaKupce.get() == 1) {
      statusPosluziteljaZaKupce.set(0);
      return "OK\n";
    }

    return "ERROR 62 – Pogrešna promjena pauze ili starta\n";
  }

  /**
   * Obradi status.
   *
   * @param linija the linija
   * @return the string
   */
  private String obradiStatus(String linija) {
    var poklapanje = predlozakStatus.matcher(linija);
    var status = poklapanje.matches();

    if (!status) {
      return "ERROR 40 - Format komande nije ispravan\n";
    }

    var kodZaAdminPartnera = this.konfig.dajPostavku("kodZaAdmin");
    var kodIzKomande = poklapanje.group(2);
    if (!kodZaAdminPartnera.equals(kodIzKomande)) {
      return "ERROR 61 – Pogrešan kodZaAdminPartnera\n";
    }

    return "OK " + statusPosluziteljaZaKupce.toString() + "\n";
  }

  /**
   * Obradi racun.
   *
   * @param linija linija
   * @return string
   */
  private String obradiRacun(String linija) {
    var odgovor = "";
    var poklapanje = predlozakRacun.matcher(linija);
    var status = poklapanje.matches();

    if (!status) {
      return "ERROR 40 - Format komande nije ispravan\n";
    }

    String korisnik = poklapanje.group(2);
    boolean postojiNarudzba = narudzbeOtvorene.containsKey(korisnik);
    if (!postojiNarudzba) {
      return "ERROR 43 - Ne postoji otvorena narudžba za korisnika/kupca\n";
    }

    placeneNarudzbe.addAll(narudzbeOtvorene.get(korisnik));
    narudzbeOtvorene.remove(korisnik);
    brojNaplacenihNarudzbi++;

    int kvotaNarudzbi = Integer.parseInt(this.konfig.dajPostavku("kvotaNarudzbi"));
    if (brojNaplacenihNarudzbi % kvotaNarudzbi == 0) {

      List<Obracun> listaObracuna = new ArrayList<>();
      for (var narudzba : placeneNarudzbe) {
        dodajObracun(listaObracuna, narudzba);
      }

      if (listaObracuna.size() == 0) {
        return "OK\n";
      }

      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      String json = gson.toJson(listaObracuna);

      odgovor = posaljiRacun(odgovor, json);
      if (odgovor.contains("ERROR")) {
        return odgovor;
      }
    } else {
      return "OK\n";
    }

    return odgovor;
  }

  /**
   * Posalji racun.
   *
   * @param odgovor the odgovor
   * @param json the json
   * @return the string
   */
  private String posaljiRacun(String odgovor, String json) {
    var id = this.konfig.dajPostavku("id");
    var sigurnosniKod = this.konfig.dajPostavku("sigKod");
    var adresa = this.konfig.dajPostavku("adresa");
    var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRad"));

    StringBuilder komanda = new StringBuilder();
    komanda.append("OBRAČUN ").append(id).append(" ").append(sigurnosniKod).append("\n")
        .append(json);

    try {
      var mreznaUticnica = new Socket(adresa, mreznaVrata);
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));

      out.write(komanda.toString());

      out.flush();
      mreznaUticnica.shutdownOutput();

      odgovor = in.readLine();
      if (odgovor.equals("OK")) {
        placeneNarudzbe.clear();
      }

      mreznaUticnica.shutdownInput();

      mreznaUticnica.close();
    } catch (IOException e) {
      return "ERROR 45 - Neuspješno slanje obračuna\n";
    }
    return odgovor;
  }

  /**
   * Dodaj obracun.
   *
   * @param listaObracuna the lista obracuna
   * @param narudzba the narudzba
   */
  private void dodajObracun(List<Obracun> listaObracuna, Narudzba narudzba) {
    boolean stavkaVecPostoji = listaObracuna.stream().anyMatch(o -> o.id().equals(narudzba.id()));
    if (stavkaVecPostoji) {
      var stavka =
          listaObracuna.stream().filter(s -> s.id().equals(narudzba.id())).findFirst().get();
      float kolicina = stavka.kolicina();
      float novaKolicina = kolicina += narudzba.kolicina();

      Obracun noviObracun = new Obracun(stavka.partner(), stavka.id(), stavka.jelo(), novaKolicina,
          stavka.cijena(), stavka.vrijeme());

      int index = IntStream.range(0, listaObracuna.size())
          .filter(i -> listaObracuna.get(i).id().equals(stavka.id())).findFirst().orElse(-1);
      listaObracuna.set(index, noviObracun);
    } else {
      int id = Integer.parseInt(this.konfig.dajPostavku("id"));
      Obracun noviObracun = new Obracun(id, narudzba.id(), narudzba.jelo(), narudzba.kolicina(),
          narudzba.cijena(), narudzba.vrijeme());
      listaObracuna.add(noviObracun);
    }
  }

  /**
   * Obradi piće.
   *
   * @param linija linija
   * @return string
   */
  private String obradiPice(String linija) {
    var poklapanje = predlozakPice.matcher(linija);
    var status = poklapanje.matches();

    if (!status) {
      return "ERROR 40 - Format komande nije ispravan\n";
    }

    String korisnik = poklapanje.group(2);
    String idPica = poklapanje.group(3);
    float kolicina = Float.parseFloat(poklapanje.group(4));

    boolean postojiNarudzba = narudzbeOtvorene.containsKey(korisnik);
    if (!postojiNarudzba) {
      return "ERROR 43 - Ne postoji otvorena narudžba za korisnika/kupca\n";
    }

    boolean postojiPice = kartaPica.containsKey(idPica);
    if (!postojiPice) {
      return "ERROR 42 - Ne postoji piće s id u kolekciji karte pića kod partnera\n";
    }

    float cijena = kartaPica.get(idPica).cijena();
    long vrijeme = System.currentTimeMillis();

    Narudzba narudzba = new Narudzba(korisnik, idPica, false, kolicina, cijena, vrijeme);
    narudzbeOtvorene.get(korisnik).add(narudzba);

    return "OK\n";
  }

  /**
   * Obradi jelo.
   *
   * @param linija linija
   * @return string
   */
  private String obradiJelo(String linija) {
    var poklapanje = predlozakJelo.matcher(linija);
    var status = poklapanje.matches();

    if (!status) {
      return "ERROR 40 - Format komande nije ispravan\n";
    }

    String korisnik = poklapanje.group(2);
    String idJela = poklapanje.group(3);
    float kolicina = Float.parseFloat(poklapanje.group(4));

    boolean postojiNarudzba = narudzbeOtvorene.containsKey(korisnik);
    if (!postojiNarudzba) {
      return "ERROR 43 - Ne postoji otvorena narudžba za korisnika/kupca\n";
    }

    boolean postojiJelo = false;
    for (var i : jelovnik.entrySet()) {
      if (i.getValue().id().equals(idJela)) {
        postojiJelo = true;
      }
    }
    if (!postojiJelo) {
      return "ERROR 41 - Ne postoji jelo s id u kolekciji jelovnika kod partnera\n";
    }

    float cijena = jelovnik.get(idJela).cijena();
    long vrijeme = System.currentTimeMillis();

    Narudzba narudzba = new Narudzba(korisnik, idJela, true, kolicina, cijena, vrijeme);
    narudzbeOtvorene.get(korisnik).add(narudzba);

    return "OK\n";
  }

  /**
   * Obradi narudžbu.
   *
   * @param linija linija
   * @return string
   */
  private String obradiNarudzbu(String linija) {
    var poklapanje = predlozakNarudzba.matcher(linija);
    var status = poklapanje.matches();

    if (!status) {
      return "ERROR 40 - Format komande nije ispravan\n";
    }

    String korisnik = poklapanje.group(1);
    boolean postoji = narudzbeOtvorene.containsKey(korisnik);
    if (!postoji) {
      narudzbeOtvorene.put(korisnik, new LinkedList<>());
    } else {
      return "ERROR 44 - Već postoji otvorena narudžba za korisnika/kupca\n";
    }

    return "OK\n";
  }

  /**
   * Obradi kartu pića.
   *
   * @param linija linija
   * @return string
   */
  private String obradiKartuPica(String linija) {
    var poklapanje = predlozakKartaPica.matcher(linija);
    var status = poklapanje.matches();

    if (!status) {
      return "ERROR 40 - Format komande nije ispravan\n";
    }

    List<KartaPica> lista = new ArrayList<>();
    for (var i : kartaPica.entrySet()) {
      lista.add(i.getValue());
    }

    Gson gson = new GsonBuilder().create();
    String json = gson.toJson(lista);

    return "OK\n" + json + "\n";
  }

  /**
   * Obradi jelovnik.
   *
   * @param linija linija
   * @return string
   */
  private String obradiJelovnik(String linija) {
    var poklapanje = predlozakJelovnik.matcher(linija);
    var status = poklapanje.matches();

    if (!status) {
      return "ERROR 40 - Format komande nije ispravan\n";
    }

    List<Jelovnik> lista = new ArrayList<>();
    for (var i : jelovnik.entrySet()) {
      lista.add(i.getValue());
    }

    Gson gson = new GsonBuilder().create();
    String json = gson.toJson(lista);

    return "OK\n" + json + "\n";
  }

  /**
   * Pošalji jelovnik.
   *
   * @param adresa adresa
   * @param mreznaVrata mrezna vrata
   * @param id id
   * @param sigurnosniKod sigurnosni kod
   * @return string
   */
  private String posaljiJelovnik(String adresa, int mreznaVrata, String id, String sigurnosniKod) {
    String linija = "";
    StringBuilder sb = new StringBuilder();
    sb.append("JELOVNIK ").append(id).append(" ").append(sigurnosniKod);

    try {
      var mreznaUticnica = new Socket(adresa, mreznaVrata);
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
      out.write(sb.toString());
      out.flush();
      mreznaUticnica.shutdownOutput();

      StringBuilder odgovor = new StringBuilder();
      while ((linija = in.readLine()) != null) {
        odgovor.append(linija).append("\n");
        if (linija.isBlank() || linija.trim().endsWith("]") || !in.ready())
          break;
      }
      linija = odgovor.toString();

      spremiJelovnik(odgovor.toString());

      mreznaUticnica.shutdownInput();
      mreznaUticnica.close();
    } catch (IOException e) {
    }
    return linija;
  }

  /**
   * Spremi jelovnik.
   *
   * @param komanda komanda
   */
  private void spremiJelovnik(String komanda) {
    String[] redovi = komanda.split("\\n", 2);

    Gson gson = new Gson();
    var jelovnikNiz = gson.fromJson(redovi[1], Jelovnik[].class);
    var jelovnikTok = Arrays.stream(jelovnikNiz);
    jelovnikTok.forEach(kp -> this.jelovnik.put(kp.id(), kp));
  }

  /**
   * Posalji kartu pića.
   *
   * @param adresa adresa
   * @param mreznaVrata mrezna vrata
   * @param id id
   * @param sigurnosniKod sigurnosni kod
   * @return string
   */
  private String posaljiKartuPica(String adresa, int mreznaVrata, String id, String sigurnosniKod) {
    String linija = "";
    StringBuilder sb = new StringBuilder();
    sb.append("KARTAPIĆA ").append(id).append(" ").append(sigurnosniKod);

    try {
      var mreznaUticnica = new Socket(adresa, mreznaVrata);
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
      out.write(sb.toString());
      out.flush();
      mreznaUticnica.shutdownOutput();

      StringBuilder odgovor = new StringBuilder();
      while ((linija = in.readLine()) != null) {
        odgovor.append(linija).append("\n");
        if (linija.isBlank() || linija.trim().endsWith("]") || !in.ready())
          break;
      }
      linija = odgovor.toString();

      spremiKartuPica(odgovor.toString());

      mreznaUticnica.shutdownInput();
      mreznaUticnica.close();
    } catch (IOException e) {
    }
    return linija;
  }

  /**
   * Spremi kartu pića.
   *
   * @param komanda komanda
   */
  private void spremiKartuPica(String komanda) {
    String[] redovi = komanda.split("\\n", 2);

    Gson gson = new Gson();
    var niz = gson.fromJson(redovi[1], KartaPica[].class);
    var tok = Arrays.stream(niz);
    tok.forEach(kp -> this.kartaPica.put(kp.id(), kp));
  }

  /**
   * Pošalji registraciju.
   */
  private void posaljiRegistraciju() {
    var id = this.konfig.dajPostavku("id").trim();
    var naziv = this.konfig.dajPostavku("naziv").trim();
    var kuhinja = this.konfig.dajPostavku("kuhinja").trim();
    var adresa = this.konfig.dajPostavku("adresa").trim();
    var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRegistracija").trim());
    var mreznaVrataKraj =
        Integer.parseInt(this.konfig.dajPostavku("mreznaVrataKrajPartner").trim());
    var gpsSirina = this.konfig.dajPostavku("gpsSirina").trim().trim();
    var gpsDuzina = this.konfig.dajPostavku("gpsDuzina").trim();
    var kodZaAdmin = this.konfig.dajPostavku("kodZaAdmin").trim();

    try {
      var mreznaUticnica = new Socket(adresa, mreznaVrata);
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));

      String adresaPartnera = mreznaUticnica.getLocalAddress().getHostAddress();
      StringBuilder komanda = new StringBuilder();
      komanda.append("PARTNER ").append(id).append(" \"").append(naziv).append("\" ")
          .append(kuhinja).append(" ").append(adresaPartnera).append(" ").append(mreznaVrata)
          .append(" ").append(gpsSirina).append(" ").append(gpsDuzina).append(" ")
          .append(mreznaVrataKraj).append(" ").append(kodZaAdmin);

      out.write(komanda.toString());
      out.flush();
      mreznaUticnica.shutdownOutput();

      var linija = in.readLine();
      if (linija.contains("OK")) {
        azurirajDatotekuPartnera(linija);
      }

      mreznaUticnica.shutdownInput();

      mreznaUticnica.close();
    } catch (IOException e) {
    }
  }

  /**
   * Ažuriraj datoteku partnera.
   *
   * @param linija linija
   */
  private void azurirajDatotekuPartnera(String linija) {
    String[] dio = linija.split(" ");
    if (this.konfig.postojiPostavka("sigKod")) {
      this.konfig.azurirajPostavku("sigKod", dio[1]);
    } else {
      this.konfig.spremiPostavku("sigKod", dio[1]);
    }
    try {
      this.konfig.spremiKonfiguraciju();
    } catch (NeispravnaKonfiguracija e) {
    }
  }

  /**
   * Pošalji kraj.
   */
  private void posaljiKraj() {
    var kodZaKraj = this.konfig.dajPostavku("kodZaKraj");
    var adresa = this.konfig.dajPostavku("adresa");
    var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataKraj"));

    try {
      var mreznaUticnica = new Socket(adresa, mreznaVrata);
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
      out.write("KRAJ " + kodZaKraj + "\n");
      out.flush();
      mreznaUticnica.shutdownOutput();
      var linija = in.readLine();
      mreznaUticnica.shutdownInput();
      if (linija.equals("OK")) {
        System.out.println("Uspješan kraj poslužitelja.");
      }
      mreznaUticnica.close();
    } catch (IOException e) {
    }
  }

  /**
   * Učitaj konfiguraciju.
   *
   * @param nazivDatoteke naziv datoteke
   * @return true, ako je uspješno učitavanje konfiguracije
   */
  private boolean ucitajKonfiguraciju(String nazivDatoteke) {
    try {
      this.konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
      return true;
    } catch (NeispravnaKonfiguracija ex) {
      Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }
}
