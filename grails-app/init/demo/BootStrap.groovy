package demo
import demo.User
import demo.Role
import demo.UserRole

class BootStrap {

  def init = { servletContext ->
      User admin = new User(username:'admin', password:'secret', enabled:true).save()
      User common = new User(username:'common', password:'secret', enabled:true).save()
      User royalty = new User(username:'royalty', password:'secret', enabled:true).save()
      Role royaltyRole = new Role(authority: 'ROLE_ROYALTY').save()
      Role commonRole = new Role(authority: 'ROLE_COMMON').save()
      UserRole.create(admin, royaltyRole, true)
      UserRole.create(admin, commonRole, true)
      UserRole.create(common, commonRole, true )

      //login
      new RequestMap(url: '/test/authenticatedPage', configAttribute: 'IS_AUTHENTICATED_FULLY').save()
      new RequestMap(url: '/test/commonPage', configAttribute: 'ROLE_COMMON').save()
      new RequestMap(url: '/test/royalPage', configAttribute: 'ROLE_ROYALTY').save()
      //anonymously
      new RequestMap(url: '/**', configAttribute: 'IS_AUTHENTICATED_ANONYMOUSLY').save()
  }
  def destroy = {
  }
}
