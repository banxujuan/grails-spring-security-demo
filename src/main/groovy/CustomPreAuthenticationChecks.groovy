package demo

import org.springframework.security.authentication.*
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsChecker

public class CustomPreAuthenticationChecks implements UserDetailsChecker
{
        // magic
        public void check(UserDetails user)
        {
        if (!user?.enabled)
        {
          throw new DisabledException("springSecurity.errors.login.disabled")
        }
        else if (!user?.accountNonExpired)
        {
          throw new AccountExpiredException("springSecurity.errors.login.expired")
        }
        else if (!user?.accountNonLocked)
        {
          throw new LockedException("springSecurity.errors.login.locked")
        }
        else if (user.loginBySms)
        {
            def now = new Date().time
            def lastPasswordModifiedDate = user.lastPasswordModifiedDate?.time
            def overdue
            if (!lastPasswordModifiedDate)
            {
                throw new CredentialsExpiredException("The password has expired")
            }
            else
            {
                overdue = ((now - lastPasswordModifiedDate) / 60000) > 10
                if (overdue)
                {
                    throw new CredentialsExpiredException("The verification code has expired")
                }
            }
        }
    }
}
