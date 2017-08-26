package com.example.demo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Document
data class User(
        @Id var id: String? = null,
        var username: String? = null,
        var password: String? = null,
        var active: Boolean = true,
        var roles: List<String> = ArrayList()
)
//    : UserDetails {
//
//    val username: String override fun get() = username
//
//    override override fun getUsername(): String {
//        return account;
//    }
//
//    override fun getPassword(): String {
//        return pwd
//    }
//
//    override fun getAuthorities(): Collection<out GrantedAuthority> {
//        return roles.map(::SimpleGrantedAuthority).toList()
//    }
//
//    override fun isAccountNonExpired(): Boolean {
//        return active;
//    }
//
//
//    override fun isAccountNonLocked(): Boolean {
//        return active
//    }
//
//    override fun isCredentialsNonExpired(): Boolean {
//        return active
//    }
//
//    override fun isEnabled(): Boolean {
//        return active
//    }
//}