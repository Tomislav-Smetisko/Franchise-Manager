package edu.unizg.foi.nwtis.tsmetisko.vjezba_08_dz_3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
import edu.unizg.foi.nwtis.podaci.Obracun;
import edu.unizg.foi.nwtis.podaci.Partner;
import edu.unizg.foi.nwtis.podaci.PartnerPopis;

/**
 * Klasa PosluziteljTvrtka.
 */
public class PosluziteljTvrtka {

  /** Konfiguracijski podaci. */
  protected Konfiguracija konfig;

  /** Pokretač dretvi. */
  private ExecutorService executor = null;

  /** Pauza dretve. */
  private int pauzaDretve = 1000;

  /** Kod za kraj rada. */
  private String kodZaKraj = "";

  /** Zastavica za kraj rada. */
  private AtomicBoolean kraj = new AtomicBoolean(false);

  /** Kuhinje. */
  private Map<String, String> kuhinje = new ConcurrentHashMap<>();

  /** Jelovnici. */
  private Map<String, Map<String, Jelovnik>> jelovnici = new ConcurrentHashMap<>();

  /** Karta pića. */
  private Map<String, KartaPica> kartaPica = new ConcurrentHashMap<>();

  /** Partneri. */
  private Map<Integer, Partner> partneri = new ConcurrentHashMap<>();

  /** Predložak registracija. */
  private Pattern predlozakRegistracija = Pattern.compile(
      "^(?<komanda>PARTNER) (?<id>\\d+) (?<partner>\\\"[^\\\"]*\\\") (?<kuhinja>\\w+) (?<adresa>(localhost|[\\d\\.]+)) (?<vrata>\\d+) (?<sirina>\\d+(\\.\\d+)?) (?<duzina>\\d+(\\.\\d+)?) (?<mreznaVrataKraj>\\d+) (?<adminKod>[\\d\\w]+)$");

  /** The predlozak obrisi. */
  private Pattern predlozakObrisi = Pattern.compile("^(OBRIŠI) (\\d+) ([\\d\\w]+)$");

  /** Predložak jelovnik. */
  private Pattern predlozakJelovnik = Pattern.compile("^(JELOVNIK) (\\d+) ([\\d\\w]+)$");

  /** Predložak karta pića. */
  private Pattern predlozakKartaPica = Pattern.compile("^(KARTAPIĆA) (\\d+) ([\\d\\w]+)$");

  /** Predložak obračun. */
  private Pattern predlozakObracun = Pattern.compile("^(OBRAČUN) (\\d+) ([\\d\\w]+)$");

  /** Predložak status. */
  private Pattern predlozakStatus = Pattern.compile("^(STATUS) ([\\d\\w]+) (1|2)$");

  /** Predložak pauza. */
  private Pattern predlozakPauza = Pattern.compile("^(PAUZA) ([\\d\\w]+) (1|2)$");

  /** Predložak start. */
  private Pattern predlozakStart = Pattern.compile("^(START) ([\\d\\w]+) (1|2)$");

  /** Predložakk spava. */
  private Pattern predlozakSpava = Pattern.compile("^(SPAVA) ([\\w\\d]+) (\\d+)$");

  /** Predložak osvježi. */
  private Pattern predlozakOsvjezi = Pattern.compile("^(OSVJEŽI) ([\\d\\w]+)$");

  /** Predložak obračun WS. */
  private Pattern predlozakObracunWS = Pattern.compile("^(OBRAČUNWS) (\\d+) ([\\d\\w]+)$");

  /** Predložak kraj WS. */
  private Pattern predlozakKrajWS = Pattern.compile("^(KRAJWS) ([\\d\\w]+)$");

  /** Drugi red. */
  private Pattern drugiRed = Pattern.compile("^\\[.*]$", Pattern.DOTALL);

  /** Jedan obračun. */
  private final Object jedanObracun = new Object();

  /** Jedan obračun WS. */
  private final Object jedanObracunWS = new Object();

  /** Status poslužitelja za rad S partnerima. */
  private AtomicInteger statusPosluziteljaZaRadSPartnerima = new AtomicInteger(1);

  /** Status poslužitelja za registraciju partnera. */
  private AtomicInteger statusPosluziteljaZaRegistracijuPartnera = new AtomicInteger(1);


  /**
   * Main metoda.
   *
   * @param args argumenti
   */
  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Broj argumenata nije 1.");
      return;
    }

    var program = new PosluziteljTvrtka();
    var nazivDatoteke = args[0];

    program.pripremiKreni(nazivDatoteke);
  }

  /**
   * Pripremi kreni.
   *
   * @param nazivDatoteke naziv datoteke
   */
  public void pripremiKreni(String nazivDatoteke) {
    if (!this.ucitajKonfiguraciju(nazivDatoteke) || !ucitajDatotekuPartnera() || !ucitajJelovnike()
        || !ucitajKartuPica()) {
      return;
    }
    this.kodZaKraj = this.konfig.dajPostavku("kodZaKraj");
    this.pauzaDretve = Integer.parseInt(this.konfig.dajPostavku("pauzaDretve"));

    var builder = Thread.ofVirtual();
    var factory = builder.factory();
    this.executor = Executors.newThreadPerTaskExecutor(factory);

    var dretvaZaKraj = this.executor.submit(() -> this.pokreniPosluziteljKraj());
    var dretvaZaRegistraciju = this.executor.submit(() -> this.pokreniPosluziteljRegistracija());
    var dretvaZaRad = this.executor.submit(() -> this.pokreniPosluziteljZaRadSPartnerima());

    while (!dretvaZaKraj.isDone()) {
      try {
        Thread.sleep(this.pauzaDretve);
      } catch (InterruptedException e) {
      }
    }
  }

  /**
   * Pokreni poslužitelj za rad S partnerima.
   */
  public void pokreniPosluziteljZaRadSPartnerima() {
    var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRad"));
    var brojCekaca = Integer.parseInt(this.konfig.dajPostavku("brojCekaca"));
    try (ServerSocket ss = new ServerSocket(mreznaVrata, brojCekaca)) {
      while (!this.kraj.get()) {
        var mreznaUticnica = ss.accept();
        this.executor.submit(() -> this.obradiRad(mreznaUticnica));
      }
      ss.close();

    } catch (IOException e) {
    }
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

      if (statusPosluziteljaZaRadSPartnerima.get() == 0) {
        out.write("ERROR 36 – Poslužitelj za partnere u pauzi\n");
        mreznaUticnica.shutdownInput();
        out.flush();
        mreznaUticnica.shutdownOutput();
        mreznaUticnica.close();
        return;
      }

      String linija = in.readLine();

      prihvatKomandi(in, out, linija);

      mreznaUticnica.shutdownInput();
      out.flush();
      mreznaUticnica.shutdownOutput();
      mreznaUticnica.close();
    } catch (Exception e) {

    }
  }

  /**
   * Prihvat komandi.
   *
   * @param in the in
   * @param out the out
   * @param linija the linija
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void prihvatKomandi(BufferedReader in, PrintWriter out, String linija)
      throws IOException {
    if (linija.toUpperCase().contains("JELOVNIK")) {
      String returnMessage = obradiJelovnik(linija);
      out.write(returnMessage);
    } else if (linija.toUpperCase().contains("KARTAPIĆA")) {
      String returnMessage = obradiKartuPica(linija);
      out.write(returnMessage);
    } else if (linija.toUpperCase().contains("OBRAČUNWS")) {
      StringBuilder komanda = new StringBuilder();
      komanda.append(linija).append("\n");
      while ((linija = in.readLine()) != null) {
        komanda.append(linija).append("\n");
        if (linija.isBlank() || linija.trim().endsWith("]") || !in.ready())
          break;
      }

      String komanda2 = komanda.toString();
      String returnMessage;
      synchronized (jedanObracunWS) {
        returnMessage = obradiObracunWS(komanda2.trim());
      }
      out.write(returnMessage);
    } else if (linija.toUpperCase().contains("OBRAČUN")) {
      StringBuilder komanda = new StringBuilder();
      komanda.append(linija).append("\n");

      while ((linija = in.readLine()) != null) {
        komanda.append(linija).append("\n");
        if (linija.isBlank() || linija.trim().endsWith("]") || !in.ready()) {
          break;
        }
      }

      String komanda2 = komanda.toString();
      String returnMessage;
      synchronized (jedanObracun) {
        returnMessage = obradiObracun(komanda2.trim());
      }
      out.write(returnMessage);
    }
  }

  /**
   * Obradi kraj WS.
   *
   * @param linija the linija
   * @return the string
   */
  private String obradiKrajWS(String linija) {
    var poklapanje = predlozakKrajWS.matcher(linija);
    var status = poklapanje.matches();

    if (!status) {
      return "ERROR 30 - Format komande nije ispravan\n";
    }

    var kodZaKraj = this.konfig.dajPostavku("kodZaKraj");
    if (!kodZaKraj.equals(poklapanje.group(2))) {
      return "ERROR 39 - Kod za kraj nije ispravan\n";
    }

    List<String> odgovoriSvihPartnera = new ArrayList<>();
    posaljiKrajSvimPartnerima(odgovoriSvihPartnera);
    for (var odgovor : odgovoriSvihPartnera) {
      if (!odgovor.equals("OK")) {
        return "ERROR 14 – Barem jedan partner nije završio rad\n";
      }
    }

    return "OK\n";
  }

  /**
   * Posalji kraj svim partnerima.
   *
   * @param odgovoriSvihPartnera the odgovori svih partnera
   */
  private void posaljiKrajSvimPartnerima(List<String> odgovoriSvihPartnera) {
    for (var partner : partneri.entrySet()) {
      try {
        Socket mreznaUticnica = new Socket();
        mreznaUticnica.connect(new InetSocketAddress(partner.getValue().adresa(),
            partner.getValue().mreznaVrataKraj()), 1000);

        BufferedReader in =
            new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
        PrintWriter out =
            new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));

        out.write("KRAJ " + this.kodZaKraj + "\n");

        out.flush();
        mreznaUticnica.shutdownOutput();

        odgovoriSvihPartnera.add(in.readLine());

        mreznaUticnica.shutdownInput();
        mreznaUticnica.close();
      } catch (IOException e) {
      }
    }
  }

  /**
   * Obradi obracun WS.
   *
   * @param linija the linija
   * @return the string
   */
  private String obradiObracunWS(String linija) {
    String[] redovi = linija.split("\\n", 2);

    var poklapanje = predlozakObracunWS.matcher(redovi[0]);
    var status = poklapanje.matches();

    if (!status || !redovi[1].trim().startsWith("[") || !redovi[1].trim().endsWith("]")) {
      return "ERROR 30 - Format komande nije ispravan\n";
    }

    List<Obracun> listaObracuna = new ArrayList<>();
    List<Obracun> listaObracuna2 = new ArrayList<>();

    var nazivDatoteke = this.konfig.dajPostavku("datotekaObracuna");
    var datoteka = Path.of(nazivDatoteke);

    if (Files.exists(datoteka) && Files.isRegularFile(datoteka) && Files.isReadable(datoteka)) {
      try (var br = Files.newBufferedReader(datoteka)) {
        Gson gson2 = new Gson();
        var obracunNiz2 = gson2.fromJson(br, Obracun[].class);
        var obracunTok2 = Arrays.stream(obracunNiz2);
        obracunTok2.forEach(kp -> listaObracuna.add(kp));
      } catch (IOException ex) {
      }
    }

    Gson gson = new Gson();
    var obracunNiz = gson.fromJson(redovi[1], Obracun[].class);
    var obracunTok = Arrays.stream(obracunNiz);
    obracunTok.forEach(kp -> listaObracuna2.add(kp));

    String prijasnjiId = "";
    int prijasnjiPartner = listaObracuna2.get(0).partner();
    for (var i : listaObracuna2) {
      if (prijasnjiId.equals(i.id()) || i.partner() != prijasnjiPartner) {
        return "ERROR 35 - Neispravan obračun\n";
      }
      prijasnjiId = i.id();
    }

    kreirajObracuneWs(listaObracuna, listaObracuna2);

    Gson gson3 = new GsonBuilder().setPrettyPrinting().create();
    String json3 = gson3.toJson(listaObracuna);

    try {
      PrintWriter writer = new PrintWriter(nazivDatoteke, "UTF-8");
      writer.println(json3);
      writer.close();
    } catch (Exception e) {
    }

    return "OK\n";
  }

  /**
   * Kreiraj obracune ws.
   *
   * @param listaObracuna the lista obracuna
   * @param listaObracuna2 the lista obracuna 2
   */
  private void kreirajObracuneWs(List<Obracun> listaObracuna, List<Obracun> listaObracuna2) {
    for (var i : listaObracuna2) {
      boolean stavkaVecPostoji = listaObracuna.stream().anyMatch(o -> o.id().equals(i.id()));
      if (stavkaVecPostoji) {
        var stavka = listaObracuna.stream().filter(s -> s.id().equals(i.id())).findFirst().get();
        if (stavka.partner() == i.partner()) {
          float kolicina = stavka.kolicina();
          float novaKolicina = kolicina += i.kolicina();
          long vrijeme = System.currentTimeMillis();

          Obracun noviObracun = new Obracun(stavka.partner(), stavka.id(), stavka.jelo(),
              novaKolicina, stavka.cijena(), vrijeme);

          int index = IntStream.range(0, listaObracuna.size())
              .filter(in -> listaObracuna.get(in).id().equals(stavka.id())).findFirst().orElse(-1);
          listaObracuna.set(index, noviObracun);
        } else {
          long vrijeme = System.currentTimeMillis();
          Obracun noviObracun =
              new Obracun(i.partner(), i.id(), i.jelo(), i.kolicina(), i.cijena(), vrijeme);
          listaObracuna.add(noviObracun);
        }
      } else {
        long vrijeme = System.currentTimeMillis();
        Obracun noviObracun =
            new Obracun(i.partner(), i.id(), i.jelo(), i.kolicina(), i.cijena(), vrijeme);
        listaObracuna.add(noviObracun);
      }
    }
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
      return "ERROR 30 - Format komande nije ispravan\n";
    }

    var kodZaAdminTvrtke = this.konfig.dajPostavku("kodZaAdminTvrtke");
    var kodIzKomande = poklapanje.group(2);
    if (!kodZaAdminTvrtke.equals(kodIzKomande)) {
      return "ERROR 12 – Pogrešan kodZaAdminTvrtke\n";
    }

    if (statusPosluziteljaZaRadSPartnerima.get() == 1) {
      if (!ucitajKartuPica()) {
        return "ERROR 39 - Neuspješno preuzimanje karte pića\n";
      }
      if (!ucitajJelovnike()) {
        return "ERROR 39 - Neuspješno preuzimanje jelovnika\n";
      }
      return "OK\n";
    }

    return "ERROR 39 - Poslužitelj za rad s partnerima je u pauzi\n";
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
      return "ERROR 30 - Format komande nije ispravan\n";
    }

    var kodZaAdminTvrtke = this.konfig.dajPostavku("kodZaAdminTvrtke");
    var kodIzKomande = poklapanje.group(2);
    if (!kodZaAdminTvrtke.equals(kodIzKomande)) {
      return "ERROR 12 – Pogrešan kodZaAdminTvrtke\n";
    }

    var duzinaSpavanja = Long.parseLong(poklapanje.group(3));
    try {
      Thread.sleep(duzinaSpavanja);
    } catch (InterruptedException e) {
      return "ERROR 16 – Prekid spavanja dretve\n";
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
      return "ERROR 30 - Format komande nije ispravan\n";
    }

    var kodZaAdminTvrtke = this.konfig.dajPostavku("kodZaAdminTvrtke");
    var kodIzKomande = poklapanje.group(2);
    if (!kodZaAdminTvrtke.equals(kodIzKomande)) {
      return "EERROR 12 – Pogrešan kodZaAdminTvrtke\n";
    }

    var odabraniPosluzitelj = poklapanje.group(3);
    if (odabraniPosluzitelj.equals("1")) {
      if (statusPosluziteljaZaRegistracijuPartnera.get() == 0) {
        statusPosluziteljaZaRegistracijuPartnera.set(1);
        return "OK\n";
      } else {
        return "ERROR 13 – Pogrešna promjena pauze ili starta\n";
      }
    } else {
      if (statusPosluziteljaZaRadSPartnerima.get() == 0) {
        statusPosluziteljaZaRadSPartnerima.set(1);
        return "OK\n";
      } else {
        return "ERROR 13 – Pogrešna promjena pauze ili starta\n";
      }
    }
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
      return "ERROR 30 - Format komande nije ispravan\n";
    }

    var kodZaAdminTvrtke = this.konfig.dajPostavku("kodZaAdminTvrtke");
    var kodIzKomande = poklapanje.group(2);
    if (!kodZaAdminTvrtke.equals(kodIzKomande)) {
      return "ERROR 12 – Pogrešan kodZaAdminTvrtke\n";
    }

    var odabraniPosluzitelj = poklapanje.group(3);
    if (odabraniPosluzitelj.equals("1")) {
      if (statusPosluziteljaZaRegistracijuPartnera.get() == 1) {
        statusPosluziteljaZaRegistracijuPartnera.set(0);
        return "OK\n";
      } else {
        return "ERROR 13 – Pogrešna promjena pauze ili starta\n";
      }
    } else {
      if (statusPosluziteljaZaRadSPartnerima.get() == 1) {
        statusPosluziteljaZaRadSPartnerima.set(0);
        return "OK\n";
      } else {
        return "ERROR 13 – Pogrešna promjena pauze ili starta\n";
      }
    }
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
      return "ERROR 30 - Format komande nije ispravan\n";
    }

    var kodZaAdminTvrtke = this.konfig.dajPostavku("kodZaAdminTvrtke");
    var kodIzKomande = poklapanje.group(2);
    if (!kodZaAdminTvrtke.equals(kodIzKomande)) {
      return "ERROR 12 – Pogrešan kodZaAdminTvrtke\n";
    }

    if (poklapanje.group(3).equals("1")) {
      return "OK " + statusPosluziteljaZaRegistracijuPartnera.get() + "\n";
    } else {
      return "OK " + statusPosluziteljaZaRadSPartnerima.get() + "\n";
    }
  }

  /**
   * Obradi obračun.
   *
   * @param linija linija
   * @return string
   */
  private String obradiObracun(String linija) {
    String[] redovi = linija.split("\\n", 2);

    var poklapanje = predlozakObracun.matcher(redovi[0]);
    var status = poklapanje.matches();

    if (!status || redovi.length < 2 || !redovi[1].trim().startsWith("[")
        || !redovi[1].trim().endsWith("]")) {
      return "ERROR 30 - Format komande nije ispravan\n";
    }

    List<Obracun> listaObracuna = new ArrayList<>();
    List<Obracun> listaObracuna2 = new ArrayList<>();

    var nazivDatoteke = this.konfig.dajPostavku("datotekaObracuna");
    var datoteka = Path.of(nazivDatoteke);

    if (Files.exists(datoteka) && Files.isRegularFile(datoteka) && Files.isReadable(datoteka)) {
      try (var br = Files.newBufferedReader(datoteka)) {
        Gson gson2 = new Gson();
        var obracunNiz2 = gson2.fromJson(br, Obracun[].class);
        var obracunTok2 = Arrays.stream(obracunNiz2);
        obracunTok2.forEach(kp -> listaObracuna.add(kp));
      } catch (IOException ex) {
      }
    }

    Gson gson = new Gson();
    var obracunNiz = gson.fromJson(redovi[1], Obracun[].class);
    var obracunTok = Arrays.stream(obracunNiz);
    obracunTok.forEach(kp -> listaObracuna2.add(kp));

    String prijasnjiId = "";
    int prijasnjiPartner = listaObracuna2.get(0).partner();
    for (var i : listaObracuna2) {
      if (prijasnjiId.equals(i.id()) || i.partner() != prijasnjiPartner) {
        return "ERROR 35 - Neispravan obračun\n";
      }
      prijasnjiId = i.id();
    }

    kreirajObracuneWs(listaObracuna, listaObracuna2);

    Gson gson3 = new GsonBuilder().setPrettyPrinting().create();
    String json3 = gson3.toJson(listaObracuna);

    try {
      PrintWriter writer = new PrintWriter(nazivDatoteke, "UTF-8");
      writer.println(json3);
      writer.close();
    } catch (Exception e) {
    }

    String odgovor = posaljiPOSTObracun(json3);
    if (odgovor.contains("ERROR")) {
      return odgovor;
    }

    return "OK\n";
  }

  /**
   * Posalji POST obracun.
   *
   * @param obracunJson the obracun json
   * @return the string
   */
  private String posaljiPOSTObracun(String obracunJson) {
    try {
      String restAdresa = this.konfig.dajPostavku("restAdresa") + "/obracun";
      HttpClient klijent = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
      HttpRequest zahtjev = HttpRequest.newBuilder().uri(URI.create(restAdresa))
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(obracunJson)).build();

      HttpResponse<Void> odgovor = klijent.send(zahtjev, HttpResponse.BodyHandlers.discarding());
      if (odgovor.statusCode() == 201) {
        return "OK\n";
      } else {
        return "ERROR 17 – RESTful zahtjev nije uspješan\n";
      }
    } catch (Exception e) {
      return "ERROR 17 – RESTful zahtjev nije uspješan\n";
    }
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
      return "ERROR 30 - Format komande nije ispravan\n";
    }

    int id = Integer.parseInt(poklapanje.group(2));
    String sigurnosniKod = poklapanje.group(3);

    boolean partnerPostoji = false;

    for (var i : partneri.entrySet()) {
      if (i.getKey().equals(id) && i.getValue().sigurnosniKod().equals(sigurnosniKod)) {
        partnerPostoji = true;
        List<KartaPica> listaPica = new ArrayList<>();
        for (var p : kartaPica.entrySet()) {
          listaPica.add(p.getValue());
        }

        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(listaPica);
        return "OK\n" + json + "\n";
      }
    }

    if (!partnerPostoji) {
      return "ERROR 31 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera\n";
    }

    return "ERROR 34 - Neispravna karta pića";
  }

  /**
   * Obradi jelovnik.
   *
   * @param linija linija
   * @return string
   */
  private String obradiJelovnik(String linija) {
    var poklapanjeJelovnik = predlozakJelovnik.matcher(linija);
    var status = poklapanjeJelovnik.matches();

    if (!status) {
      return "ERROR 30 - Format komande nije ispravan\n";
    }

    int id = Integer.parseInt(poklapanjeJelovnik.group(2));
    String sigurnosniKod = poklapanjeJelovnik.group(3);

    boolean partnerPostoji = false;
    boolean jelovnikPostoji = false;
    for (var i : partneri.entrySet()) {
      if (i.getKey().equals(id) && i.getValue().sigurnosniKod().equals(sigurnosniKod)) {
        partnerPostoji = true;
        String kuhinja = i.getValue().vrstaKuhinje();
        for (var j : jelovnici.entrySet()) {
          if (j.getKey().equals(kuhinja)) {
            jelovnikPostoji = true;
            List<Jelovnik> listaJelovnika = new ArrayList<>();
            var jelovnik = j.getValue();

            for (var l : jelovnik.entrySet()) {
              listaJelovnika.add(l.getValue());
            }

            Gson gson = new GsonBuilder().create();
            String json = gson.toJson(listaJelovnika);
            return "OK\n" + json + "\n";
          }
        }
      }
    }

    if (!partnerPostoji) {
      return "ERROR 31 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera\n";
    }
    if (!jelovnikPostoji) {
      return "ERROR 32 - Ne postoji jelovnik s vrstom kuhinje koju partner ima ugovorenu\n";
    }

    return "ERROR 33 - Neispravan jelovnik";
  }

  /**
   * Pokreni poslužitelj registracija.
   */
  public void pokreniPosluziteljRegistracija() {
    var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRegistracija"));
    var brojCekaca = 10;
    try (ServerSocket ss = new ServerSocket(mreznaVrata, brojCekaca)) {
      while (!this.kraj.get()) {
        var mreznaUticnica = ss.accept();
        this.executor.submit(() -> this.obradiRegistraciju(mreznaUticnica));
      }
      ss.close();

    } catch (IOException e) {
    }
  }

  /**
   * Obradi registraciju.
   *
   * @param mreznaUticnica mrezna uticnica
   */
  public void obradiRegistraciju(Socket mreznaUticnica) {
    try {
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));

      if (statusPosluziteljaZaRegistracijuPartnera.get() == 0) {
        out.write("ERROR 24 – Poslužitelj za registraciju partnera u pauzi\n");
        out.flush();
        mreznaUticnica.shutdownOutput();
        mreznaUticnica.close();
        return;
      }

      String linija = in.readLine();
      mreznaUticnica.shutdownInput();

      if (linija.contains("PARTNER")) {
        String returnMessage = provjeraRegistracije(linija);
        out.write(returnMessage);
      } else if (linija.contains("OBRIŠI")) {
        String returnMessage = brisanjePartnera(linija);
        out.write(returnMessage);
      } else if (linija.equals("POPIS")) {
        String returnMessage = popis();
        out.write("OK\n" + returnMessage);
      } else {
        System.err.println("ERROR 20 - Format komande nije ispravan");
      }

      out.flush();
      mreznaUticnica.shutdownOutput();
      mreznaUticnica.close();
    } catch (Exception e) {

    }
  }

  /**
   * Popis.
   *
   * @return string
   */
  private String popis() {
    List<PartnerPopis> listaPartnera = new ArrayList<PartnerPopis>();
    for (var p : partneri.values()) {
      PartnerPopis partnerPopis = new PartnerPopis(p.id(), p.naziv(), p.vrstaKuhinje(), p.adresa(),
          p.mreznaVrata(), p.gpsSirina(), p.gpsDuzina());
      listaPartnera.add(partnerPopis);
    }

    Gson gson = new GsonBuilder().create();
    String json = gson.toJson(listaPartnera);
    return json;
  }

  /**
   * Brisanje partnera.
   *
   * @param linija linija
   * @return string
   */
  private String brisanjePartnera(String linija) {
    var poklapanje = predlozakObrisi.matcher(linija);
    var status = poklapanje.matches();

    if (!status) {
      return "ERROR 30 - Format komande nije ispravan\n";
    }

    String returnMessage = "";
    int id = Integer.parseInt(poklapanje.group(2));
    String sigurnosniKod = poklapanje.group(3);
    boolean postojiID = false;
    boolean ispravanSigKod = false;

    for (var element : partneri.entrySet()) {
      if (element.getKey() == id) {
        postojiID = true;
        if (element.getValue().sigurnosniKod().equals(sigurnosniKod)) {
          ispravanSigKod = true;
          partneri.remove(id);
          returnMessage = "OK\n";
          spremiPartnereUJson();
        }
      }
    }
    if (!postojiID) {
      returnMessage =
          "ERROR 23 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera\n";
    } else if (!ispravanSigKod) {
      returnMessage = "ERROR 22 - Neispravan sigurnosni kod partnera\n";
    }

    return returnMessage;
  }

  /**
   * Provjera registracije.
   *
   * @param linija linija
   * @return string
   */
  private String provjeraRegistracije(String linija) {
    if (statusPosluziteljaZaRegistracijuPartnera.get() == 0) {
      return "ERROR 24 – Poslužitelj za registraciju partnera u pauzi\n";
    }

    var poklapanjeRegistracija = predlozakRegistracija.matcher(linija);
    var statusRegistracija = poklapanjeRegistracija.matches();

    if (!statusRegistracija) {
      return "ERROR 20 - Format komande nije ispravan\n";
    }

    int id = Integer.parseInt(poklapanjeRegistracija.group("id"));
    var nazivSNavodnicima = poklapanjeRegistracija.group("partner");
    String naziv = nazivSNavodnicima.substring(1, nazivSNavodnicima.length() - 1);
    String kuhinja = poklapanjeRegistracija.group("kuhinja");
    String adresa = poklapanjeRegistracija.group("adresa");
    int vrata = Integer.parseInt(poklapanjeRegistracija.group("vrata"));
    int mreznaVrataKraj = Integer.parseInt(poklapanjeRegistracija.group("mreznaVrataKraj"));
    float sirina = Float.parseFloat(poklapanjeRegistracija.group("sirina"));
    float duzina = Float.parseFloat(poklapanjeRegistracija.group("duzina"));
    String adminKod = poklapanjeRegistracija.group("adminKod");

    boolean postoji = false;
    if (partneri.containsKey(id)) {
      postoji = true;
    }

    boolean kuhinjaPostoji = this.konfig.dajSvePostavke().entrySet().stream()
        .filter(e -> ((String) e.getKey()).startsWith("kuhinja_"))
        .anyMatch(e -> ((String) e.getValue()).contains(kuhinja));

    if (!kuhinjaPostoji) {
      return "ERROR 29 - Registracija za nepostojeću kuhinju\n";
    }

    if (!postoji) {
      String spojeno = naziv + adresa;
      int hashirano = spojeno.hashCode();
      String sigurnosniKod = Integer.toHexString(hashirano);

      Partner partner = new Partner(id, naziv, kuhinja, adresa, vrata, sirina, duzina,
          mreznaVrataKraj, sigurnosniKod, adminKod);

      partneri.put(partner.id(), partner);
      spremiPartnereUJson();
      return "OK " + partner.sigurnosniKod() + "\n";
    } else {
      return "ERROR 21 - Već postoji partner s id u kolekciji partnera\n";
    }
  }

  /**
   * Pokreni poslužitelj kraj.
   */
  public void pokreniPosluziteljKraj() {
    var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataKraj"));
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
   * Obradi komandu kraj.
   *
   * @param linija the linija
   * @return the string
   */
  private String obradiKomanduKraj(String linija) {
    List<String> odgovoriSvihPartnera2 = new ArrayList<>();
    posaljiKrajSvimPartnerima(odgovoriSvihPartnera2);
    for (var odgovor : odgovoriSvihPartnera2) {
      if (!odgovor.equals("OK")) {
        return "ERROR 14 – Barem jedan partner nije završio rad\n";
      }
    }

    try {
      String restAdresa = this.konfig.dajPostavku("restAdresa") + "/kraj/info";
      HttpClient klijent = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
      HttpRequest zahtjev = HttpRequest.newBuilder().uri(URI.create(restAdresa))
          .method("HEAD", HttpRequest.BodyPublishers.noBody()).build();

      HttpResponse<Void> odgovor = klijent.send(zahtjev, HttpResponse.BodyHandlers.discarding());

      if (odgovor.statusCode() != 200) {
        return "ERROR 17 – RESTful zahtjev nije uspješan\n";
      }
    } catch (Exception e) {
      return "ERROR 17 – RESTful zahtjev nije uspješan\n";
    }

    return "OK\n";
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

      prihvatKomandiNaPosluziteljuKraj(out, linija);

      out.flush();
      mreznaUticnica.shutdownOutput();
      mreznaUticnica.close();
    } catch (Exception e) {

    }
    return Boolean.TRUE;
  }

  /**
   * Prihvat komandi na posluzitelju kraj.
   *
   * @param out the out
   * @param linija the linija
   */
  private void prihvatKomandiNaPosluziteljuKraj(PrintWriter out, String linija) {
    if (linija.trim().equals("KRAJ " + this.kodZaKraj)) {
      String returnMessage = obradiKomanduKraj(linija);
      out.write(returnMessage);
      if (returnMessage.equals("OK\n")) {
        this.kraj.set(true);
      }
    } else if (linija.toUpperCase().contains("KRAJWS")) {
      String returnMessage = obradiKrajWS(linija);
      out.write(returnMessage);
      if (returnMessage.equals("OK\n")) {
        this.kraj.set(true);
      }
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
  }

  /**
   * Spremi partnere U json.
   */
  public void spremiPartnereUJson() {
    var naziv = this.konfig.dajPostavku("datotekaPartnera");

    List<Partner> listaPartnera = new ArrayList<Partner>();
    for (var p : partneri.values()) {
      listaPartnera.add(p);
    }

    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String json = gson.toJson(listaPartnera);

    try {
      PrintWriter writer = new PrintWriter(naziv, "UTF-8");
      writer.println(json);
      writer.close();
    } catch (Exception e) {
    }
  }

  /**
   * Učitaj datoteku partnera.
   *
   * @return true, ako su partneri uspješno učitani
   */
  public boolean ucitajDatotekuPartnera() {
    var nazivDatotekePartnera = this.konfig.dajPostavku("datotekaPartnera");
    var datoteka = Path.of(nazivDatotekePartnera);

    try (var br = Files.newBufferedReader(datoteka)) {
      Gson gson = new Gson();
      var partneriNiz = gson.fromJson(br, Partner[].class);
      var partneriTok = Arrays.stream(partneriNiz);
      partneriTok.forEach(kp -> this.partneri.put(kp.id(), kp));
    } catch (IOException ex) {
    }

    return true;
  }

  /**
   * Učitaj jelovnike.
   *
   * @return true, ako su jelovnici uspješno učitani
   */
  public boolean ucitajJelovnike() {
    var svePostavke = this.konfig.dajSvePostavke();
    var kljucevi = svePostavke.keySet();
    for (var i : kljucevi) {
      if (i.toString().contains("kuhinja_")) {
        var datotekaKuhinje = Path.of(i.toString() + ".json");
        if (!Files.exists(datotekaKuhinje) || !Files.isRegularFile(datotekaKuhinje)
            || !Files.isReadable(datotekaKuhinje)) {
          continue;
        }

        kuhinje.put(i.toString(), this.konfig.dajPostavku(i.toString()));
      }
    }

    for (var kuh : kuhinje.entrySet()) {
      var nazivDatoteke = kuh.getKey().toString();
      var datoteka = Path.of(nazivDatoteke + ".json");
      if (!Files.exists(datoteka) || !Files.isRegularFile(datoteka)
          || !Files.isReadable(datoteka)) {
        return false;
      }

      try (var br = Files.newBufferedReader(datoteka)) {
        Gson gson = new Gson();
        var jelovnikNiz = gson.fromJson(br, Jelovnik[].class);
        var jelovnikTok = Arrays.stream(jelovnikNiz);
        Map<String, Jelovnik> jelovnik = new ConcurrentHashMap<>();
        jelovnikTok.forEach(kp -> jelovnik.put(kp.id(), kp));

        String[] value = kuh.getValue().split(";");
        jelovnici.put(value[0], jelovnik);
      } catch (IOException ex) {
      }
    }

    return true;
  }

  /**
   * Učitaj kartu pića.
   *
   * @return true, ako je karta pića uspješno učitana
   */
  public boolean ucitajKartuPica() {
    var nazivDatotekePica = this.konfig.dajPostavku("datotekaKartaPica");
    var datoteka = Path.of(nazivDatotekePica);
    if (!Files.exists(datoteka) || !Files.isRegularFile(datoteka) || !Files.isReadable(datoteka)) {
      return false;
    }

    try (var br = Files.newBufferedReader(datoteka)) {
      Gson gson = new Gson();
      var kartaPicaNiz = gson.fromJson(br, KartaPica[].class);
      var kartaPicaTok = Arrays.stream(kartaPicaNiz);
      kartaPicaTok.forEach(kp -> this.kartaPica.put(kp.id(), kp));
    } catch (IOException ex) {
      return false;
    }

    return true;
  }

  /**
   * Učitaj konfiguraciju.
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

  /**
   * Gets the karta pica.
   *
   * @return the karta pica
   */
  public Map<String, KartaPica> getKartaPica() {
    return this.kartaPica;
  }

  /**
   * Gets the jelovnik.
   *
   * @return the jelovnik
   */
  public Map<String, Map<String, Jelovnik>> getJelovnik() {
    return this.jelovnici;
  }

  /**
   * Gets the partnere.
   *
   * @return the partnere
   */
  public Map<Integer, Partner> getPartnere() {
    return this.partneri;
  }
}
