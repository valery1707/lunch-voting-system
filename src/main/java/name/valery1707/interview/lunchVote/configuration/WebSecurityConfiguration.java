package name.valery1707.interview.lunchVote.configuration;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;

import javax.inject.Inject;

@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
	@Value("${security.enable-csrf}")
	private boolean enableCsrf;

	@Value("${securityRoleHierarchy}")
	private String roleHierarchy;

	@Inject
	private AuthenticationService authenticationService;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(authenticationService);
		provider.setPasswordEncoder(new Md5PasswordEncoder());
		auth.authenticationProvider(provider);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		super.configure(http);
		if (!enableCsrf) {
			http.csrf().disable();
		}
		http.authorizeRequests().expressionHandler(makeExpressionHandler());
	}

	@NotNull
	private DefaultWebSecurityExpressionHandler makeExpressionHandler() {
		DefaultWebSecurityExpressionHandler expressionHandler = new DefaultWebSecurityExpressionHandler();
		expressionHandler.setRoleHierarchy(makeRoleHierarchy(roleHierarchy));
		return expressionHandler;
	}

	@NotNull
	public static RoleHierarchyImpl makeRoleHierarchy(String hierarchy) {
		RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
		roleHierarchy.setHierarchy(hierarchy);
		return roleHierarchy;
	}
}
