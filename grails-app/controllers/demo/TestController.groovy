package demo
// import org.springframework.security.access.annotation.Secured
//
// @Secured(['permitAll'])
class TestController {

  def publicPage() {
      render "This is a public page"
  }
  def authenticatedPage() {
      render "This is a authenticated only page"
      session.invalidate()
  }
  def commonPage() {
      render "This is a common role page"
  }
  def royalPage() {
      render "This is a royal role page"
  }
}
