package name.valery1707.interview.lunchVote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.ZonedDateTime;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

@Configuration
@EnableWebMvc
@ComponentScan
public class Launcher {
	private static final Logger LOG = LoggerFactory.getLogger(Launcher.class);

	@Bean
	public EmbeddedServletContainerFactory embeddedServletContainerFactory() {
		return new JettyEmbeddedServletContainerFactory();
	}

	@Bean
	public DispatcherServlet dispatcherServlet() {
		return new DispatcherServlet();
	}

	public static void main(String[] args) {
		LOG.info("Launcher started at {}", ZonedDateTime.now().format(ISO_OFFSET_DATE_TIME));
		SpringApplication.run(Launcher.class, args);
	}
}