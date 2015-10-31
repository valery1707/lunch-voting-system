package name.valery1707.interview.lunchVote.configuration;

import name.valery1707.interview.lunchVote.api.AccountRepo;
import name.valery1707.interview.lunchVote.domain.Account;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

@Component
@Scope(scopeName = SCOPE_SINGLETON)
public class AuthenticationService implements UserDetailsService {
	public static final SimpleGrantedAuthority ROLE_USER = new SimpleGrantedAuthority("ROLE_USER");
	public static final SimpleGrantedAuthority ROLE_ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");
	private static final Set<SimpleGrantedAuthority> AUTH_USER = Collections.singleton(ROLE_USER);
	private static final Set<SimpleGrantedAuthority> AUTH_ADMIN = Collections.singleton(ROLE_ADMIN);

	@Inject
	private AccountRepo repo;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		List<Account> accounts = repo.findByLogin(username);
		if (accounts.isEmpty()) {
			throw new UsernameNotFoundException(String.format("Username %s not found", username));
		}
		Account account = accounts.get(0);
		return new User(account.getLogin(), account.getPassword(), account.isAdmin() ? AUTH_ADMIN : AUTH_USER);
	}
}
