package name.valery1707.interview.lunchVote.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

import javax.annotation.PostConstruct;

import static java.util.Collections.singletonList;
import static name.valery1707.interview.lunchVote.configuration.WebSecurityConfiguration.makeRoleHierarchy;

@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {
	@Value("${securityRoleHierarchy}")
	private String roleHierarchy;

	@PostConstruct
	public void postConstruct() {
		DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
		handler.setRoleHierarchy(makeRoleHierarchy(roleHierarchy));
		setMethodSecurityExpressionHandler(singletonList(handler));
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		super.configure(auth);
	}
}
