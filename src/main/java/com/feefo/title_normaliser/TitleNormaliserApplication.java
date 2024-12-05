package com.feefo.title_normaliser;

import java.util.Optional;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TitleNormaliserApplication {

	public static void main(String[] args) {
		String jt = "Java engineer";
		Normaliser n = new Normaliser();
		Optional<String> normalisedTitle = n.normalise(jt);
		//output normalisedTitle
		System.out.println(normalisedTitle);
		jt = "C# engineer";
		normalisedTitle = n.normalise(jt);
		//output normalisedTitle
		System.out.println(normalisedTitle);
		jt = "Chief Accountant";
		normalisedTitle = n.normalise(jt);
		//output normalisedTitle
		System.out.println(normalisedTitle);
	}

}
