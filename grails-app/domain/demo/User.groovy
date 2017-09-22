package demo

import grails.plugin.springsecurity.SpringSecurityService
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
@EqualsAndHashCode(includes='username')
@ToString(includes='username', includeNames=true, includePackage=false)
class User implements Serializable {

	private static final long serialVersionUID = 1

	SpringSecurityService springSecurityService

	String username
	String password
	boolean enabled = true
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired

	String cellphone
	String verificationCode

	Date lastPasswordModifiedDate

	Boolean loginBySms = false

	Boolean fixedIp = false

	String ip

	Set<Role> getAuthorities() {
		(UserRole.findAllByUser(this) as List<UserRole>)*.role as Set<Role>
	}

	def beforeInsert() {
		encodePassword()
	}

	def beforeUpdate() {
		if (isDirty('password')) {
			encodePassword()
		}
	}

	protected void encodePassword() {
		password = springSecurityService?.passwordEncoder ? springSecurityService.encodePassword(password) : password
	}

	static transients = ['springSecurityService']

	static constraints = {
				username blank: false, unique: true
        password blank: false, password: true, minSize: 3

        cellphone size: 11..11, blank: true, nullable: true, unique: true
        verificationCode blank: true, nullable: true, maxSize: 16
				lastPasswordModifiedDate blank: true, nullable: true
        ip blank: true, nullable: true, maxSize: 64
    }

	static mapping = {
		password column: '`password`'
	}
}
