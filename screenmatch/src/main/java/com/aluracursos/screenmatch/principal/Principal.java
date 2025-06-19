package com.aluracursos.screenmatch.principal;

import com.aluracursos.screenmatch.model.DatosEpisodio;
import com.aluracursos.screenmatch.model.DatosSerie;
import com.aluracursos.screenmatch.model.DatosTemporadas;
import com.aluracursos.screenmatch.model.Episodio;
import com.aluracursos.screenmatch.service.ConsumoApi;
import com.aluracursos.screenmatch.service.ConvierteDatos;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner teclado = new Scanner(System.in );

    private ConsumoApi consumoApi = new ConsumoApi();
    private ConvierteDatos conversor = new ConvierteDatos();
    private final String URL_BASE = "https://www.omdbapi.com/?t=";
    private final String SEPARADOR_APIKEY = "&apikey=";
    private final String API_KEY = System.getenv("OMDB_API_KEY");

    public void muestraElMenu(){

        System.out.println("\nIngrese el nombre de la serie que desea buscar: \n");


        var nombreSerie = teclado.nextLine();
        System.out.println();

        System.out.println("---DATOS GENERALES DE LA SERIE---");
        var json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + SEPARADOR_APIKEY + API_KEY);
        System.out.println(json);



        System.out.println("\n---CONVIERTE DATOS---");
        DatosSerie datos = conversor.obtenerDatos(json, DatosSerie.class);
        System.out.println(datos);



        //Busca los datos de todas las temporadas
        System.out.println("\n---DATOS DE LA TEMPORADA---");
        List<DatosTemporadas> temporadas = new ArrayList<>();

        for (int i = 1; i <= datos.totalDeTemporadas(); i++) {
            json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + "&Season=" + i + SEPARADOR_APIKEY + API_KEY);
            DatosTemporadas datosTemporada = conversor.obtenerDatos(json, DatosTemporadas.class);
            temporadas.add(datosTemporada);
        }
        temporadas.forEach(System.out::println);


        //Mostrar solo el titulo de los episodios para las temporadas
        /*for (int i = 0; i < datos.totalDeTemporadas(); i++) {
            List<DatosEpisodio> episodiosTemporadas = temporadas.get(i).episodios();
            for (int j = 0; j < episodiosTemporadas.size(); j++) {
                System.out.println(episodiosTemporadas.get(j).titulo());
            }
        }*/

        // Mejoría usando funciones Lambda
        System.out.println("\n\n---TITULO DE LOS EPISODIOS---");
        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

        //Convertir todas las informaciones a una lista del tipo DatosEpisodio
        List<DatosEpisodio> datosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());


        // Obtener los top 5 episodios  funcion peek para mensajes filtro
        System.out.println("\n---TOP 5 EPISODIOS---");
        datosEpisodios.stream()
                .filter(e -> !e.evaluacion().equalsIgnoreCase("N/A"))
                .peek(e -> System.out.println("Primer filtro N/A " +e))
                .sorted(Comparator.comparing(DatosEpisodio::evaluacion).reversed())
                .peek(e -> System.out.println("Ordenando los datos " + e))
                .limit(5)
                .peek(e -> System.out.println("Limitando a 5 " + e))
                .map(e -> e.titulo().toUpperCase())
                .peek(e -> System.out.println("Mayusculas " + e))
                .forEach(System.out::println);


        //Convirtiendo los datos a una lista del tipo Episodio
        System.out.println("\n---DATOS EPISODIOS---");
        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d-> new Episodio(t.numero(),d))
                        )
                .collect(Collectors.toList());
        episodios.forEach(System.out::println);

        // Busqueda de episodios a partir de x año
        System.out.println("\na partir de que año deseas ver los episodios?\n");
        var fecha = teclado.nextInt();
        teclado.nextLine();

        LocalDate fechaBusqueda = LocalDate.of(fecha, 1, 1);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        episodios.stream()
                .filter(e -> e.getFechaDeLanzamiento() != null && e.getFechaDeLanzamiento().isAfter(fechaBusqueda))
                .forEach(e -> System.out.println(
                        "Temporada: " + e.getTemporada() +
                                " Episodio: " + e.getTitulo() +
                                " Fecha de Lanzamiento: " + e.getFechaDeLanzamiento().format(dtf)
                ));

        //Busqueda de un episodio por su titulo
        System.out.println("\nEscriba el titulo del episodio que desea ver \n");
        var pedazoTitulo = teclado.nextLine();
        Optional<Episodio> episodioBuscado = episodios.stream()
                .filter(e -> e.getTitulo().toUpperCase().contains(pedazoTitulo.toUpperCase()))
                .findFirst();
        if (episodioBuscado.isPresent()){
            System.out.println("\nEpisodio encontrado");
            System.out.println("Temporada: " + episodioBuscado.get());
        } else {
            System.out.println("Episodio no encontrado");
        }

        Map<Integer , Double> evaluacionesPorTemporada = episodios.stream()
                .filter(e -> e.getEvaluacion() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                        Collectors.averagingDouble(Episodio::getEvaluacion)));
        System.out.println("\n---EVALUACIONES POR TEMPORADA---");
        System.out.println(evaluacionesPorTemporada);


        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getEvaluacion() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getEvaluacion));
        System.out.println("\n---ESTADISTICAS---\n " );
        System.out.println("Media de las evaluaciones: " + est.getAverage());
        System.out.println("Episodio Mejor evaluado: " + est.getMax());
        System.out.println("Episodio Peor evaluado: " + est.getMin());

    }
}
