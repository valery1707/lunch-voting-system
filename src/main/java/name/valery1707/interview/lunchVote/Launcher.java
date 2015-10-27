package name.valery1707.interview.lunchVote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.ZonedDateTime;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public class Launcher {
	private static final Logger LOG = LoggerFactory.getLogger(Launcher.class);

	public static void main(String[] args) {
		LOG.info("Launcher started at {}", ZonedDateTime.now().format(ISO_OFFSET_DATE_TIME));
	}
}