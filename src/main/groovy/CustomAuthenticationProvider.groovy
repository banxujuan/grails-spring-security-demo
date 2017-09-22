package demo

import groovy.transform.CompileStatic
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

@CompileStatic
public class CustomAuthenticationProvider extends DaoAuthenticationProvider
{
    protected void additionalAuthenticationChecks(UserDetails userDetails,
        UsernamePasswordAuthenticationToken authentication) throws AuthenticationException
    {
        // super.additionalAuthenticationChecks(userDetails, authentication)
        Object details = authentication.details
        if (!(userDetails instanceof CustomUserDetails))
        {
            println "provider not customUserDetails"
        }
        if (!(details instanceof CustomWebAuthenticationDetails))
        {
            println "provider not customAuthenticationDetails"
        }
        def customWebAuthenticationDetails = details as CustomWebAuthenticationDetails
        def user = userDetails as CustomUserDetails

        // additionalAuthenticationChecks
        Object salt = null;

    		if (this.saltSource != null) {
    			salt = this.saltSource.getSalt(userDetails);
    		}

    		if (authentication.getCredentials() == null) {
    			logger.debug("Authentication failed: no credentials provided");

    			throw new BadCredentialsException(messages.getMessage(
    					"AbstractUserDetailsAuthenticationProvider.badCredentials",
    					"Bad credentials"));
    		}

    		String presentedPassword = authentication.getCredentials().toString();

        def loginMethod = customWebAuthenticationDetails.getLoginMethod()
        if (loginMethod == "phone")
        // if (loginMethod == null)
        {
          if (!(user.verificationCode == presentedPassword))
          {
            logger.debug("Authentication failed: password does not match stored value");
            throw new BadCredentialsException(messages.getMessage(
              "AbstractUserDetailsAuthenticationProvider.badCredentials",
              "Bad credentials"));
          }
        }
        else
        {
          if (!passwordEncoder.isPasswordValid(userDetails.getPassword(),
          presentedPassword, salt)) {
            logger.debug("Authentication failed: password does not match stored value");
            throw new BadCredentialsException(messages.getMessage(
              "AbstractUserDetailsAuthenticationProvider.badCredentials",
              "Bad credentials"));
            }
        }

        //TODO
        if (user.fixedIp)
        {
            if (user.ip != customWebAuthenticationDetails.getRemoteAddr())
            {
                throw new CustomIpFixedException("User ip fiexed")
            }
        }
    }

    public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		Assert.isInstanceOf(UsernamePasswordAuthenticationToken.class, authentication,
				messages.getMessage(
						"AbstractUserDetailsAuthenticationProvider.onlySupports",
						"Only UsernamePasswordAuthenticationToken is supported"));

		// Determine username
		String username = (authentication.getPrincipal() == null) ? "NONE_PROVIDED"
				: authentication.getName();

		boolean cacheWasUsed = true;
		UserDetails user = this.userCache.getUserFromCache(username);

		if (user == null) {
			cacheWasUsed = false;

			try {
				user = retrieveUser(username,
						(UsernamePasswordAuthenticationToken) authentication);
			}
			catch (UsernameNotFoundException notFound) {
				logger.debug("User '" + username + "' not found");

				if (hideUserNotFoundExceptions) {
					throw new BadCredentialsException(messages.getMessage(
							"AbstractUserDetailsAuthenticationProvider.badCredentials",
							"Bad credentials"));
				}
				else {
					throw notFound;
				}
			}

			Assert.notNull(user,
					"retrieveUser returned null - a violation of the interface contract");
		}

		try {
			preAuthenticationChecks.check(user);
			additionalAuthenticationChecks(user,
					(UsernamePasswordAuthenticationToken) authentication);
		}
		catch (AuthenticationException exception) {
			if (cacheWasUsed) {
				// There was a problem, so try again after checking
				// we're using latest data (i.e. not from the cache)
				cacheWasUsed = false;
				user = retrieveUser(username,
						(UsernamePasswordAuthenticationToken) authentication);
				preAuthenticationChecks.check(user);
				additionalAuthenticationChecks(user,
						(UsernamePasswordAuthenticationToken) authentication);
			}
			else {
				throw exception;
			}
		}

		postAuthenticationChecks.check(user);
		if (!cacheWasUsed) {
			this.userCache.putUserInCache(user);
		}

		Object principalToReturn = user;

		if (forcePrincipalAsString) {
			principalToReturn = user.getUsername();
		}

		return createSuccessAuthentication(principalToReturn, authentication, user);
	}

}
