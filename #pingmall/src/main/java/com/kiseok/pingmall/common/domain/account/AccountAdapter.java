package com.kiseok.pingmall.common.domain.account;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class AccountAdapter extends User {

    private final Account account;

    public AccountAdapter(Account account) {
        super(account.getEmail(), account.getPassword(), authorities(account));
        this.account = account;
    }

    private static Collection<? extends GrantedAuthority> authorities(Account account) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + account.getAccountRole().name()));
        return grantedAuthorities;
    }
}
