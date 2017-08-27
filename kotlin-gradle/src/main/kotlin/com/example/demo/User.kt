package com.example.demo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.UserDetails


@Document
data class User(
        @Id var id: String? = null,
        var username: String? = null,
        var password: String? = null,
        var active: Boolean = true,
        var roles: List<String> = ArrayList()
)

//@Document
//data class User(
//        @Id var id: String? = null,
//        private var username: String? = null,
//        private var password: String? = null,
//        var active: Boolean = true,
//        var roles: List<String> = ArrayList()
//) : UserDetails {
//
//    override fun getUsername() = username
//
//    override fun getPassword() = password
//
//    override fun isAccountNonExpired() = active
//
//    override fun isAccountNonLocked()= active
//
//    override fun isCredentialsNonExpired()= active
//
//    override fun isEnabled()= active
//
//    override fun getAuthorities(): Collection<out GrantedAuthority> {
//        // return roles.map(::SimpleGrantedAuthority).toList()
//        return AuthorityUtils.createAuthorityList(* roles.toTypedArray())
//    }
//
//}