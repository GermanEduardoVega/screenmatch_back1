package com.aluracursos.screenmatch;

import ch.qos.logback.core.util.LocationUtil;
import com.aluracursos.screenmatch.service.ConsumoApi;
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
	}
}
