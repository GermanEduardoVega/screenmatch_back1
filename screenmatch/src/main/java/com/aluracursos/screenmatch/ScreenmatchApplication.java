package com.aluracursos.screenmatch;

import ch.qos.logback.core.util.LocationUtil;
import com.aluracursos.screenmatch.model.DatosEpisodio;
import com.aluracursos.screenmatch.model.DatosSerie;
import com.aluracursos.screenmatch.service.ConsumoApi;
import com.aluracursos.screenmatch.service.ConvierteDatos;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ScreenmatchApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ScreenmatchApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("Consumiendo datos de la serie");

		var consumoApi = new ConsumoApi();
		var apikey = System.getenv("OMDB_API_KEY");	//configurar la variable de entorno
		var json = consumoApi.obtenerDatos("https://www.omdbapi.com/?t=game+of+thrones&apikey=" + apikey );
		System.out.println(json);

		System.out.println("\n---CONVIERTE DATOS---");
		ConvierteDatos conversor = new ConvierteDatos();
		var datos = conversor.obtenerDatos(json, DatosSerie.class);
		System.out.println(datos);

		System.out.println("\n----Consumo Api especifica de los episodios---");
		json = consumoApi.obtenerDatos("https://www.omdbapi.com/?t=game+of+thrones&Season=1&episode=1&apikey=" + apikey);
		DatosEpisodio episodios = conversor.obtenerDatos(json, DatosEpisodio.class);
		System.out.println(episodios);
	}
}
